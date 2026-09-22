import React, { useState } from 'react';
import { DlqEntry } from '../types';
import { ShieldAlert, RefreshCw, AlertTriangle, CheckCircle } from 'lucide-react';

interface DlqManagerProps {
  dlqEntries: DlqEntry[];
  onRetryDlq: (id: number) => void;
}

export const DlqManager: React.FC<DlqManagerProps> = ({ dlqEntries, onRetryDlq }) => {
  const [retryingId, setRetryingId] = useState<number | null>(null);

  const handleRetry = async (id: number) => {
    setRetryingId(id);
    try {
      await onRetryDlq(id);
    } finally {
      setRetryingId(null);
    }
  };

  return (
    <div className="glass-panel rounded-xl p-5 border border-slate-800">
      <div className="flex items-center justify-between pb-4 mb-4 border-b border-slate-800">
        <div className="flex items-center space-x-2">
          <ShieldAlert className="h-5 w-5 text-rose-400" />
          <div>
            <h2 className="text-base font-bold text-white">Dead Letter Queue (DLQ) Manager</h2>
            <p className="text-xs text-slate-400">
              Stores orders that exceeded maximum bounded retries (3 attempts). Allows administrator manual inspection & retry.
            </p>
          </div>
        </div>

        <span className="px-3 py-1 text-xs font-bold rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30">
          {dlqEntries.length} Dead Letters Total
        </span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-900/60 text-slate-400 uppercase text-[10px] font-semibold tracking-wider border-b border-slate-800">
            <tr>
              <th className="py-3 px-4">DLQ ID</th>
              <th className="py-3 px-4">Order ID</th>
              <th className="py-3 px-4">Failure Reason</th>
              <th className="py-3 px-4">Retry Attempts</th>
              <th className="py-3 px-4">Last Error Exception</th>
              <th className="py-3 px-4">Moved To DLQ At</th>
              <th className="py-3 px-4 text-right">Admin Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {dlqEntries.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-12 text-center text-slate-500">
                  <div className="flex flex-col items-center justify-center space-y-2">
                    <CheckCircle className="h-8 w-8 text-emerald-500/40" />
                    <span className="text-sm font-medium text-slate-400">Dead Letter Queue is Empty</span>
                    <span className="text-xs text-slate-600">No unrecoverable order failures recorded.</span>
                  </div>
                </td>
              </tr>
            ) : (
              dlqEntries.map(dlq => (
                <tr key={dlq.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3 px-4 font-mono font-bold text-rose-400">#DLQ-{dlq.id}</td>
                  <td className="py-3 px-4 font-mono font-bold text-white">#{dlq.orderId}</td>
                  <td className="py-3 px-4 text-slate-300 font-medium">{dlq.failureReason}</td>
                  <td className="py-3 px-4 font-mono text-rose-400 font-bold">{dlq.retryCount} / 3</td>
                  <td className="py-3 px-4 font-mono text-slate-400 text-[11px] max-w-xs truncate" title={dlq.lastError}>
                    {dlq.lastError || 'N/A'}
                  </td>
                  <td className="py-3 px-4 text-slate-400 font-mono text-[11px]">
                    {new Date(dlq.createdAt).toLocaleTimeString()}
                  </td>
                  <td className="py-3 px-4 text-right">
                    <button
                      onClick={() => handleRetry(dlq.id)}
                      disabled={retryingId === dlq.id}
                      className="inline-flex items-center space-x-1 px-3 py-1 text-xs font-semibold rounded bg-brand-cyan text-black hover:bg-cyan-300 disabled:opacity-50 transition-all shadow-md shadow-brand-cyan/10"
                    >
                      <RefreshCw className={`h-3.5 w-3.5 ${retryingId === dlq.id ? 'animate-spin' : ''}`} />
                      <span>{retryingId === dlq.id ? 'Re-queueing...' : 'Retry Order'}</span>
                    </button>
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
