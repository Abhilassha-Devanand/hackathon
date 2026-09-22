import React from 'react';
import { Cpu, Zap, ShoppingCart, ShieldAlert, BarChart3, Radio, RefreshCw } from 'lucide-react';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  sseConnected: boolean;
  activeWorkers: number;
  queueSize: number;
  onRefresh: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  sseConnected,
  activeWorkers,
  queueSize,
  onRefresh
}) => {
  const tabs = [
    { id: 'overview', label: 'Overview', icon: BarChart3 },
    { id: 'orders', label: 'Live Orders', icon: ShoppingCart },
    { id: 'inventory', label: 'Inventory & Contention', icon: Zap },
    { id: 'workers', label: 'Worker Concurrency', icon: Cpu },
    { id: 'dlq', label: 'DLQ Manager', icon: ShieldAlert },
    { id: 'simulator', label: 'Concurrency Simulator', icon: Radio, highlight: true },
  ];

  return (
    <header className="sticky top-0 z-50 glass-panel border-b border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Title */}
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-brand-cyan to-brand-indigo flex items-center justify-center shadow-lg shadow-brand-cyan/20">
              <Zap className="h-6 w-6 text-white animate-pulse" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-lg font-bold tracking-tight text-white font-mono">ORDER<span className="text-brand-cyan">PULSE</span></h1>
                <span className="px-2 py-0.5 text-xs font-semibold bg-brand-cyan/10 text-brand-cyan rounded-full border border-brand-cyan/30">v1.0 Hackathon Edition</span>
              </div>
              <p className="text-xs text-slate-400">Concurrent Order Engine & Adaptive Guard</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="hidden md:flex space-x-1">
            {tabs.map(tab => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-slate-800 text-white shadow-inner border border-slate-700/80'
                      : tab.highlight
                      ? 'text-brand-cyan hover:bg-brand-cyan/10 border border-brand-cyan/30'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                  }`}
                >
                  <Icon className={`h-4 w-4 ${isActive ? 'text-brand-cyan' : ''}`} />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Right Live Indicators */}
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-900/60 border border-slate-800 text-xs">
              <span className={`h-2 w-2 rounded-full ${sseConnected ? 'bg-emerald-400 animate-ping' : 'bg-rose-500'}`} />
              <span className="text-slate-300 font-mono text-[11px]">{sseConnected ? 'LIVE STREAM' : 'DISCONNECTED'}</span>
            </div>

            <div className="hidden lg:flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-900/60 border border-slate-800 text-xs font-mono">
              <Cpu className="h-3.5 w-3.5 text-brand-cyan" />
              <span className="text-slate-400">Workers:</span>
              <span className="text-brand-cyan font-bold">{activeWorkers}</span>
              <span className="text-slate-600">|</span>
              <span className="text-slate-400">Queue:</span>
              <span className="text-brand-amber font-bold">{queueSize}</span>
            </div>

            <button
              onClick={onRefresh}
              className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors border border-slate-700"
              title="Manual Refresh"
            >
              <RefreshCw className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
