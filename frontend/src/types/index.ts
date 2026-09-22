export type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'RESERVED'
  | 'CONFIRMED'
  | 'FAILED'
  | 'RETRYING'
  | 'OUT_OF_STOCK'
  | 'CANCELLED'
  | 'DLQ';

export type ContentionLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Product {
  id: number;
  name: string;
  sku: string;
  price: number;
  description: string;
}

export interface Inventory {
  productId: number;
  productName: string;
  sku: string;
  availableQuantity: number;
  reservedQuantity: number;
  contentionLevel: ContentionLevel;
  requestsPerSecond: number;
  successfulAllocations: number;
  failedAllocations: number;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: number;
  customerId: string;
  status: OrderStatus;
  totalAmount: number;
  idempotencyKey?: string;
  retryCount: number;
  workerId?: string;
  lastError?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface WorkerStatus {
  workerId: string;
  status: 'IDLE' | 'PROCESSING';
  currentOrderId?: number;
  startedAtEpochMs?: number;
}

export interface ThreadPoolMetrics {
  corePoolSize: number;
  maximumPoolSize: number;
  activeThreads: number;
  idleThreads: number;
  queueSize: number;
  completedTaskCount: number;
  failedTaskCount: number;
  averageTaskDurationMs: number;
  workers: WorkerStatus[];
}

export interface SystemMetrics {
  totalOrders: number;
  successfulOrders: number;
  failedOrders: number;
  outOfStockOrders: number;
  processingOrders: number;
  retryingOrders: number;
  dlqOrders: number;
  activeWorkers: number;
  queueSize: number;
  ordersPerSecond: number;
  avgProcessingTimeMs: number;
}

export interface DlqEntry {
  id: number;
  orderId: number;
  failureReason: string;
  retryCount: number;
  payload: string;
  lastError: string;
  createdAt: string;
}

export interface ConcurrentTestRequest {
  productId: number;
  numberOfOrders: number;
  quantityPerOrder: number;
  threadPoolSize?: number;
  failureSimulationPercentage?: number;
  initialStockOverride?: number;
}

export interface ConcurrentTestResponse {
  testName: string;
  productId: number;
  initialStock: number;
  finalStock: number;
  totalAttempted: number;
  successful: number;
  outOfStock: number;
  failed: number;
  sentToDlq: number;
  executionDurationMs: number;
  peakOrdersPerSecond: number;
  inventorySafetyVerified: boolean;
  message: string;
}

export interface SystemEvent {
  eventType: string;
  orderId?: number;
  productId?: number;
  data: any;
  timestamp: string;
}
