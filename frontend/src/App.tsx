import React, { useState, useEffect, useCallback } from 'react';
import { Navbar } from './components/Navbar';
import { MetricsOverview } from './components/MetricsOverview';
import { WorkerGrid } from './components/WorkerGrid';
import { LiveOrdersTable } from './components/LiveOrdersTable';
import { InventoryMonitor } from './components/InventoryMonitor';
import { ContentionChart } from './components/ContentionChart';
import { DlqManager } from './components/DlqManager';
import { ConcurrencySimulator } from './components/ConcurrencySimulator';
import { api, connectEventStream } from './services/api';
import {
  Product,
  Inventory,
  Order,
  SystemMetrics,
  ThreadPoolMetrics,
  DlqEntry,
  SystemEvent
} from './types';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('simulator');
  const [sseConnected, setSseConnected] = useState<boolean>(false);

  const [products, setProducts] = useState<Product[]>([]);
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);
  const [threadPoolMetrics, setThreadPoolMetrics] = useState<ThreadPoolMetrics | null>(null);
  const [dlqEntries, setDlqEntries] = useState<DlqEntry[]>([]);
  
  const [contentionHistory, setContentionHistory] = useState<Array<{
    time: string;
    requestsPerSec: number;
    activeWorkers: number;
    confirmedOrders: number;
  }>>([]);

  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const fetchAllData = useCallback(async () => {
    try {
      const [prodRes, invRes, ordRes, sysRes, poolRes, dlqRes] = await Promise.all([
        api.getProducts(),
        api.getInventory(),
        api.getOrders(0, 100),
        api.getSystemMetrics(),
        api.getThreadPoolMetrics(),
        api.getDlq()
      ]);

      setProducts(prodRes);
      setInventoryList(invRes);
      setOrders(ordRes.content);
      setMetrics(sysRes);
      setThreadPoolMetrics(poolRes);
      setDlqEntries(dlqRes);

      // Add timeline data point for chart
      const timeStr = new Date().toLocaleTimeString();
      const maxReqSec = invRes.length > 0 ? Math.max(...invRes.map(i => i.requestsPerSecond)) : 0;
      
      setContentionHistory(prev => [
        ...prev.slice(-19), // Keep last 20 points
        {
          time: timeStr,
          requestsPerSec: maxReqSec,
          activeWorkers: poolRes.activeThreads,
          confirmedOrders: sysRes.successfulOrders
        }
      ]);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    }
  }, []);

  useEffect(() => {
    fetchAllData();
    const interval = setInterval(fetchAllData, 2000); // Poll every 2s as safety fallback
    return () => clearInterval(interval);
  }, [fetchAllData]);

  // Connect Server-Sent Events for instant live pushes
  useEffect(() => {
    const disconnect = connectEventStream((event: SystemEvent) => {
      if (event.eventType === 'CONNECTED') {
        setSseConnected(true);
      } else {
        fetchAllData(); // Refresh UI instantly on real-time event
        showToast(`${event.eventType}: Order #${event.orderId || ''}`);
      }
    });

    return () => {
      disconnect();
      setSseConnected(false);
    };
  }, [fetchAllData]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleUpdateStock = async (productId: number, quantity: number) => {
    await api.updateStock(productId, quantity);
    fetchAllData();
  };

  const handleCancelOrder = async (id: number) => {
    await api.cancelOrder(id);
    fetchAllData();
  };

  const handleUpdatePoolSize = async (size: number) => {
    await api.updateThreadPoolSize(size);
    fetchAllData();
  };

  const handleRetryDlq = async (id: number) => {
    await api.retryDlqOrder(id);
    fetchAllData();
  };

  return (
    <div className="min-h-screen bg-dark-bg text-slate-100 flex flex-col">
      {/* Header Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        sseConnected={sseConnected}
        activeWorkers={threadPoolMetrics?.activeThreads || 0}
        queueSize={threadPoolMetrics?.queueSize || 0}
        onRefresh={fetchAllData}
      />

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        
        {/* Top Summary Metrics Bar */}
        <MetricsOverview metrics={metrics} />

        {/* Tab Content rendering */}
        {activeTab === 'overview' && (
          <div className="space-y-6">
            <WorkerGrid metrics={threadPoolMetrics} onUpdatePoolSize={handleUpdatePoolSize} />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <InventoryMonitor inventoryList={inventoryList} onUpdateStock={handleUpdateStock} />
              <ContentionChart data={contentionHistory} />
            </div>
            <LiveOrdersTable orders={orders} onCancelOrder={handleCancelOrder} />
          </div>
        )}

        {activeTab === 'orders' && (
          <LiveOrdersTable orders={orders} onCancelOrder={handleCancelOrder} />
        )}

        {activeTab === 'inventory' && (
          <div className="space-y-6">
            <InventoryMonitor inventoryList={inventoryList} onUpdateStock={handleUpdateStock} />
            <ContentionChart data={contentionHistory} />
          </div>
        )}

        {activeTab === 'workers' && (
          <WorkerGrid metrics={threadPoolMetrics} onUpdatePoolSize={handleUpdatePoolSize} />
        )}

        {activeTab === 'dlq' && (
          <DlqManager dlqEntries={dlqEntries} onRetryDlq={handleRetryDlq} />
        )}

        {activeTab === 'simulator' && (
          <ConcurrencySimulator products={products} onRefreshAll={fetchAllData} />
        )}
      </main>

      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-4 right-4 z-50 px-4 py-2.5 rounded-xl bg-slate-900 border border-brand-cyan/40 text-xs text-brand-cyan shadow-xl font-mono animate-bounce">
          ⚡ {toastMessage}
        </div>
      )}

      {/* Footer */}
      <footer className="border-t border-slate-800/60 py-4 text-center text-xs text-slate-500 font-mono">
        Concurrent Order Processing System & Adaptive Inventory Guard &copy; 2026 Hackathon Edition
      </footer>
    </div>
  );
};
