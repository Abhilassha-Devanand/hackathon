import React from 'react';
import { SystemMetrics } from '../types';
import { ShoppingBag, CheckCircle2, AlertTriangle, RefreshCw, ShieldAlert, Cpu, Activity, Clock } from 'lucide-react';

interface MetricsOverviewProps {
  metrics: SystemMetrics | null;
}

export const MetricsOverview: React.FC<MetricsOverviewProps> = ({ metrics }) => {
  if (!metrics) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 animate-pulse">
        {[...Array(8)].map((_, i) => (
          <div key={i} className="h-28 rounded-xl bg-slate-800/40 border border-slate-800" />
        ))}
      </div>
    );
  }

  const successRate = metrics.totalOrders > 0
    ? ((metrics.successfulOrders / metrics.totalOrders) * 100).toFixed(1)
    : '0.0';

  const cards = [
    {
      title: 'Total Orders',
      value: metrics.totalOrders.toLocaleString(),
      subtext: `${successRate}% Success Rate`,
      icon: ShoppingBag,
      color: 'text-brand-cyan',
      bg: 'bg-brand-cyan/10 border-brand-cyan/20'
    },
    {
      title: 'Successful Orders',
      value: metrics.successfulOrders.toLocaleString(),
      subtext: 'Stock Reserved & Confirmed',
      icon: CheckCircle2,
      color: 'text-emerald-400',
      bg: 'bg-emerald-500/10 border-emerald-500/20'
    },
    {
      title: 'Out of Stock',
      value: metrics.outOfStockOrders.toLocaleString(),
      subtext: 'Protected Against Overselling',
      icon: AlertTriangle,
      color: 'text-amber-400',
      bg: 'bg-amber-500/10 border-amber-500/20'
    },
    {
      title: 'Retrying Orders',
      value: metrics.retryingOrders.toLocaleString(),
      subtext: 'Exponential Backoff Active',
      icon: RefreshCw,
      color: 'text-indigo-400',
      bg: 'bg-indigo-500/10 border-indigo-500/20'
    },
    {
      title: 'Dead Letter Queue',
      value: metrics.dlqOrders.toLocaleString(),
      subtext: 'Max Retries Reached',
      icon: ShieldAlert,
      color: 'text-rose-400',
      bg: 'bg-rose-500/10 border-rose-500/20'
    },
    {
      title: 'Active Workers',
      value: `${metrics.activeWorkers} Threads`,
      subtext: `Queue: ${metrics.queueSize} Tasks`,
      icon: Cpu,
      color: 'text-purple-400',
      bg: 'bg-purple-500/10 border-purple-500/20'
    },
    {
      title: 'Orders / Second',
      value: `${metrics.ordersPerSecond.toFixed(1)} req/s`,
      subtext: 'Live Throughput Rate',
      icon: Activity,
      color: 'text-sky-400',
      bg: 'bg-sky-500/10 border-sky-500/20'
    },
    {
      title: 'Avg Task Duration',
      value: `${metrics.avgProcessingTimeMs.toFixed(1)} ms`,
      subtext: 'Per Worker Execution',
      icon: Clock,
      color: 'text-teal-400',
      bg: 'bg-teal-500/10 border-teal-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div key={idx} className="glass-card rounded-xl p-4 border relative overflow-hidden group">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium text-slate-400">{card.title}</span>
              <div className={`p-2 rounded-lg ${card.bg}`}>
                <Icon className={`h-4 w-4 ${card.color}`} />
              </div>
            </div>
            <div className="mt-2">
              <span className="text-2xl font-bold font-mono text-white tracking-tight">{card.value}</span>
            </div>
            <div className="mt-1 flex items-center text-[11px] text-slate-500 font-medium">
              <span>{card.subtext}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
};
