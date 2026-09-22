import {
  Product,
  Inventory,
  Order,
  SystemMetrics,
  ThreadPoolMetrics,
  DlqEntry,
  ConcurrentTestRequest,
  ConcurrentTestResponse,
  SystemEvent
} from '../types';

const API_BASE = '/api';

async function fetchJSON<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers
    },
    ...options
  });
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`API Error (${response.status}): ${errorText}`);
  }
  return response.json();
}

export const api = {
  getProducts: () => fetchJSON<Product[]>(`${API_BASE}/products`),
  
  getInventory: () => fetchJSON<Inventory[]>(`${API_BASE}/inventory`),
  
  updateStock: (productId: number, quantity: number) =>
    fetchJSON<Inventory>(`${API_BASE}/inventory/${productId}/stock?quantity=${quantity}`, { method: 'POST' }),

  getOrders: (page = 0, size = 50) =>
    fetchJSON<{ content: Order[]; totalElements: number }>(`${API_BASE}/orders?page=${page}&size=${size}`),

  createOrder: (data: { customerId: string; productId: number; quantity: number; idempotencyKey?: string }) =>
    fetchJSON<Order>(`${API_BASE}/orders`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  cancelOrder: (id: number) =>
    fetchJSON<Order>(`${API_BASE}/orders/${id}/cancel`, { method: 'POST' }),

  getSystemMetrics: () => fetchJSON<SystemMetrics>(`${API_BASE}/system/metrics`),

  getThreadPoolMetrics: () => fetchJSON<ThreadPoolMetrics>(`${API_BASE}/system/threadpool`),

  updateThreadPoolSize: (size: number) =>
    fetchJSON<ThreadPoolMetrics>(`${API_BASE}/system/threadpool/size?size=${size}`, { method: 'POST' }),

  getDlq: () => fetchJSON<DlqEntry[]>(`${API_BASE}/dlq`),

  retryDlqOrder: (id: number) =>
    fetchJSON<{ message: string }>(`${API_BASE}/dlq/${id}/retry`, { method: 'POST' }),

  updateSimulationConfig: (config: { enabled: boolean; failureRatePercentage: number; transientFailuresOnly: boolean }) =>
    fetchJSON(`${API_BASE}/system/simulation`, {
      method: 'POST',
      body: JSON.stringify(config)
    }),

  runConcurrentTest: (request: ConcurrentTestRequest) =>
    fetchJSON<ConcurrentTestResponse>(`${API_BASE}/test/concurrent-orders`, {
      method: 'POST',
      body: JSON.stringify(request)
    })
};

export function connectEventStream(onEvent: (event: SystemEvent) => void): () => void {
  const eventSource = new EventSource(`${API_BASE}/system/stream`);

  const eventTypes = [
    'CONNECTED',
    'ORDER_CREATED',
    'ORDER_PROCESSING',
    'INVENTORY_RESERVED',
    'ORDER_CONFIRMED',
    'ORDER_FAILED',
    'ORDER_RETRYING',
    'ORDER_OUT_OF_STOCK',
    'ORDER_SENT_TO_DLQ',
    'ORDER_CANCELLED',
    'INVENTORY_UPDATED',
    'CONTENTION_CHANGED'
  ];

  eventTypes.forEach(type => {
    eventSource.addEventListener(type, (e: MessageEvent) => {
      try {
        const payload: SystemEvent = JSON.parse(e.data);
        onEvent(payload);
      } catch (err) {
        console.error('Error parsing SSE event:', err);
      }
    });
  });

  eventSource.onerror = (err) => {
    console.warn('SSE EventSource error:', err);
  };

  return () => {
    eventSource.close();
  };
}
