import React, { useState } from 'react';
import { ThreadPoolMetrics } from '../types';
import { Cpu, CheckCircle, AlertCircle, Layers, Sliders } from 'lucide-react';

interface WorkerGridProps {
  metrics: ThreadPoolMetrics | null;
  onUpdatePoolSize: (size: number) => void;
}

export const WorkerGrid: React.FC<WorkerGridProps> = ({ metrics, onUpdatePoolSize }) => {
  const [sliderValue, setSliderValue] = useState<number>(metrics?.corePoolSize || 10);
  const [isUpdating, setIsUpdating] = useState(false);

  const handleApplyPoolSize = async () => {
    setIsUpdating(true);
    try {
      await onUpdatePoolSize(sliderValue);
    } finally {
      setIsUpdating(false);
    }
  };

  if (!metrics) {
    return <div className="h-64 glass-panel rounded-xl animate-pulse" />;
  }

  return (
    <div className="glass-panel rounded-xl p-5 border border-slate-800">
      {/* Header & Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between pb-4 mb-4 border-b border-slate-800 gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <Cpu className="h-5 w-5 text-brand-cyan" />
            <h2 className="text-base font-bold text-white">Thread Pool Concurrency Engine</h2>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Manages parallel order processing via configurable Java <code className="text-brand-cyan">ThreadPoolExecutor</code>
          </p>
        </div>

        {/* Thread Pool Size Controller */}
        <div className="flex items-center space-x-3 bg-slate-900/80 p-2.5 rounded-xl border border-slate-800">
          <Sliders className="h-4 w-4 text-slate-400" />
          <span className="text-xs text-slate-300 font-medium whitespace-nowrap">Pool Size:</span>
          <input
            type="range"
            min="1"
            max="50"
            value={sliderValue}
            onChange={e => setSliderValue(Number(e.target.value))}
            className="w-24 accent-brand-cyan cursor-pointer"
          />
          <span className="font-mono text-sm font-bold text-brand-cyan w-6 text-center">{sliderValue}</span>
          <button
            onClick={handleApplyPoolSize}
            disabled={isUpdating || sliderValue === metrics.corePoolSize}
            className="px-3 py-1 text-xs font-semibold rounded-lg bg-brand-cyan text-black hover:bg-cyan-300 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
          >
            {isUpdating ? 'Applying...' : 'Set Pool Size'}
          </button>
        </div>
      </div>

      {/* Overview Sub-stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-5">
        <div className="bg-slate-900/60 rounded-lg p-3 border border-slate-800/80">
          <span className="text-[11px] text-slate-400 font-medium">Active Threads</span>
          <div className="text-lg font-bold font-mono text-brand-cyan mt-0.5">{metrics.activeThreads} / {metrics.corePoolSize}</div>
        </div>
        <div className="bg-slate-900/60 rounded-lg p-3 border border-slate-800/80">
          <span className="text-[11px] text-slate-400 font-medium">Order Task Queue</span>
          <div className="text-lg font-bold font-mono text-brand-amber mt-0.5">{metrics.queueSize} Queued</div>
        </div>
        <div className="bg-slate-900/60 rounded-lg p-3 border border-slate-800/80">
          <span className="text-[11px] text-slate-400 font-medium">Completed Worker Tasks</span>
          <div className="text-lg font-bold font-mono text-emerald-400 mt-0.5">{metrics.completedTaskCount}</div>
        </div>
        <div className="bg-slate-900/60 rounded-lg p-3 border border-slate-800/80">
          <span className="text-[11px] text-slate-400 font-medium">Failed Worker Tasks</span>
          <div className="text-lg font-bold font-mono text-rose-400 mt-0.5">{metrics.failedTaskCount}</div>
        </div>
      </div>

      {/* Workers Visual Grid */}
      <div>
        <h3 className="text-xs font-semibold text-slate-400 mb-3 flex items-center space-x-1.5">
          <Layers className="h-3.5 w-3.5" />
          <span>Worker Thread Pool Grid</span>
        </h3>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 max-h-80 overflow-y-auto pr-1">
          {metrics.workers.map(worker => {
            const isProcessing = worker.status === 'PROCESSING';
            return (
              <div
                key={worker.workerId}
                className={`p-3 rounded-xl border transition-all ${
                  isProcessing
                    ? 'bg-brand-cyan/10 border-brand-cyan/40 shadow-lg shadow-brand-cyan/10'
                    : 'bg-slate-900/40 border-slate-800/60 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between mb-1.5">
                  <span className="text-xs font-bold font-mono text-slate-200">{worker.workerId}</span>
                  <span
                    className={`h-2 w-2 rounded-full ${
                      isProcessing ? 'bg-brand-cyan animate-ping' : 'bg-slate-600'
                    }`}
                  />
                </div>

                {isProcessing ? (
                  <div>
                    <div className="flex items-center space-x-1 text-[11px] text-brand-cyan font-medium">
                      <span className="truncate">Order #{worker.currentOrderId}</span>
                    </div>
                    <span className="text-[10px] text-slate-400 block mt-0.5 font-mono">Status: Processing</span>
                  </div>
                ) : (
                  <div>
                    <span className="text-[11px] text-slate-500 font-medium">IDLE</span>
                    <span className="text-[10px] text-slate-600 block mt-0.5 font-mono">Awaiting task</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
