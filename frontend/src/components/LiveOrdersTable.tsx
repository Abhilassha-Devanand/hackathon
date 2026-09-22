import React, { useState } from 'react';
import { Order, OrderStatus } from '../types';
import { ShoppingCart, Search, Filter, Ban, AlertCircle } from 'lucide-react';

interface LiveOrdersTableProps {
  orders: Order[];
  onCancelOrder: (id: number) => void;
}

export const LiveOrdersTable: React.FC<LiveOrdersTableProps> = ({ orders, onCancelOrder }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const filteredOrders = orders.filter(order => {
    const matchesSearch =
      order.id.toString().includes(searchTerm) ||
      order.customerId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (order.workerId && order.workerId.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusBadge = (status: OrderStatus) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">CONFIRMED</span>;
      case 'OUT_OF_STOCK':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30">OUT OF STOCK</span>;
      case 'PROCESSING':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-brand-cyan/10 text-brand-cyan border border-brand-cyan/30 animate-pulse">PROCESSING</span>;
      case 'RETRYING':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">RETRYING</span>;
      case 'DLQ':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30">DLQ</span>;
      case 'RESERVED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/30">RESERVED</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-800 text-slate-400 border border-slate-700">CANCELLED</span>;
      default:
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-800 text-slate-300">PENDING</span>;
    }
  };

  return (
    <div className="glass-panel rounded-xl p-5 border border-slate-800">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 mb-4 border-b border-slate-800 gap-3">
        <div className="flex items-center space-x-2">
          <ShoppingCart className="h-5 w-5 text-brand-cyan" />
          <div>
            <h2 className="text-base font-bold text-white">Live Orders Stream</h2>
            <p className="text-xs text-slate-400">Real-time status updates broadcast via Server-Sent Events (SSE)</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          {/* Search */}
          <div className="relative">
            <Search className="h-3.5 w-3.5 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search Order # or Customer..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              className="pl-9 pr-3 py-1.5 bg-slate-900/80 border border-slate-800 rounded-lg text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-cyan"
            />
          </div>

          {/* Filter */}
          <div className="flex items-center space-x-1.5 bg-slate-900/80 border border-slate-800 px-2.5 py-1.5 rounded-lg">
            <Filter className="h-3.5 w-3.5 text-slate-400" />
            <select
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="bg-transparent text-xs text-slate-300 focus:outline-none cursor-pointer"
            >
              <option value="ALL" className="bg-slate-900">All Statuses</option>
              <option value="CONFIRMED" className="bg-slate-900">CONFIRMED</option>
              <option value="OUT_OF_STOCK" className="bg-slate-900">OUT OF STOCK</option>
              <option value="PROCESSING" className="bg-slate-900">PROCESSING</option>
              <option value="RETRYING" className="bg-slate-900">RETRYING</option>
              <option value="DLQ" className="bg-slate-900">DLQ</option>
              <option value="CANCELLED" className="bg-slate-900">CANCELLED</option>
            </select>
          </div>
        </div>
      </div>

      {/* Orders Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-900/60 text-slate-400 uppercase text-[10px] font-semibold tracking-wider border-b border-slate-800">
            <tr>
              <th className="py-3 px-4">Order ID</th>
              <th className="py-3 px-4">Customer</th>
              <th className="py-3 px-4">Total Amount</th>
              <th className="py-3 px-4">Status</th>
              <th className="py-3 px-4">Worker Thread</th>
              <th className="py-3 px-4">Retries</th>
              <th className="py-3 px-4">Created At</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {filteredOrders.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-8 text-center text-slate-500">
                  No orders match the current search filter.
                </td>
              </tr>
            ) : (
              filteredOrders.map(order => (
                <tr key={order.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3 px-4 font-mono font-bold text-white">#{order.id}</td>
                  <td className="py-3 px-4 font-mono text-slate-300">{order.customerId}</td>
                  <td className="py-3 px-4 font-mono text-emerald-400 font-semibold">${order.totalAmount.toFixed(2)}</td>
                  <td className="py-3 px-4">{getStatusBadge(order.status)}</td>
                  <td className="py-3 px-4 font-mono text-xs">
                    {order.workerId ? (
                      <span className="px-2 py-0.5 rounded bg-slate-800 text-brand-cyan font-semibold border border-slate-700">
                        {order.workerId}
                      </span>
                    ) : (
                      <span className="text-slate-600">-</span>
                    )}
                  </td>
                  <td className="py-3 px-4 font-mono">
                    {order.retryCount > 0 ? (
                      <span className="text-indigo-400 font-bold">{order.retryCount} / 3</span>
                    ) : (
                      <span className="text-slate-600">0</span>
                    )}
                  </td>
                  <td className="py-3 px-4 text-slate-400 font-mono text-[11px]">
                    {new Date(order.createdAt).toLocaleTimeString()}
                  </td>
                  <td className="py-3 px-4 text-right">
                    {(order.status === 'CONFIRMED' || order.status === 'RESERVED') && (
                      <button
                        onClick={() => onCancelOrder(order.id)}
                        className="inline-flex items-center space-x-1 px-2.5 py-1 text-[11px] font-medium text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded border border-rose-500/20 transition-all"
                      >
                        <Ban className="h-3 w-3" />
                        <span>Cancel</span>
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
