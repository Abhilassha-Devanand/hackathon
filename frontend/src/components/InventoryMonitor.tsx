import React, { useState } from 'react';
import { Inventory, ContentionLevel } from '../types';
import { Zap, ShieldCheck, AlertOctagon, Flame, Plus, Edit3 } from 'lucide-react';

interface InventoryMonitorProps {
  inventoryList: Inventory[];
  onUpdateStock: (productId: number, quantity: number) => void;
}

export const InventoryMonitor: React.FC<InventoryMonitorProps> = ({ inventoryList, onUpdateStock }) => {
  const [editingStockId, setEditingStockId] = useState<number | null>(null);
  const [newStockValue, setNewStockValue] = useState<number>(0);

  const getContentionBadge = (level: ContentionLevel) => {
    switch (level) {
      case 'HIGH':
        return (
          <span className="inline-flex items-center space-x-1 px-3 py-1 text-xs font-bold rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/40 contention-high-glow">
            <Flame className="h-3.5 w-3.5 animate-bounce" />
            <span>HIGH CONTENTION</span>
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="inline-flex items-center space-x-1 px-3 py-1 text-xs font-bold rounded-full bg-amber-500/15 text-amber-400 border border-amber-500/30">
            <AlertOctagon className="h-3.5 w-3.5" />
            <span>MEDIUM CONTENTION</span>
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center space-x-1 px-3 py-1 text-xs font-semibold rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <ShieldCheck className="h-3.5 w-3.5" />
            <span>LOW CONTENTION</span>
          </span>
        );
    }
  };

  const handleSaveStock = (productId: number) => {
    onUpdateStock(productId, newStockValue);
    setEditingStockId(null);
  };

  return (
    <div className="glass-panel rounded-xl p-5 border border-slate-800">
      <div className="flex items-center justify-between pb-4 mb-4 border-b border-slate-800">
        <div className="flex items-center space-x-2">
          <Zap className="h-5 w-5 text-brand-cyan" />
          <div>
            <h2 className="text-base font-bold text-white">Adaptive Inventory Guard & Stock Monitor</h2>
            <p className="text-xs text-slate-400">
              Dynamically evaluates inventory contention per product to prevent database deadlocks and overselling
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {inventoryList.map(item => {
          const isEditing = editingStockId === item.productId;
          const totalStock = item.availableQuantity + item.reservedQuantity;
          const stockPercentage = totalStock > 0 ? (item.availableQuantity / totalStock) * 100 : 0;

          return (
            <div
              key={item.productId}
              className="glass-card rounded-xl p-4 border border-slate-800/80 hover:border-brand-cyan/40 transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-sm font-bold text-white tracking-tight">{item.productName}</h3>
                    <span className="text-[11px] font-mono text-slate-400">{item.sku}</span>
                  </div>
                  {getContentionBadge(item.contentionLevel)}
                </div>

                {/* Stock Level Bar */}
                <div className="mt-4">
                  <div className="flex justify-between items-center text-xs mb-1.5 font-mono">
                    <span className="text-slate-400">Available Stock:</span>
                    <span className={`font-bold ${item.availableQuantity <= 5 ? 'text-rose-400' : 'text-emerald-400'}`}>
                      {item.availableQuantity} items
                    </span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-900 overflow-hidden border border-slate-800">
                    <div
                      className={`h-full transition-all duration-500 ${
                        item.availableQuantity <= 5 ? 'bg-rose-500' : 'bg-brand-cyan'
                      }`}
                      style={{ width: `${Math.min(stockPercentage, 100)}%` }}
                    />
                  </div>
                  <div className="flex justify-between text-[10px] font-mono text-slate-500 mt-1">
                    <span>Reserved: {item.reservedQuantity}</span>
                    <span>Requests/sec: {item.requestsPerSecond.toFixed(1)}</span>
                  </div>
                </div>
              </div>

              {/* Stats & Edit Stock Button */}
              <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between">
                <div className="text-[11px] font-mono text-slate-400">
                  <span className="text-emerald-400 font-bold">{item.successfulAllocations}</span> Allocations |{' '}
                  <span className="text-amber-400 font-bold">{item.failedAllocations}</span> Out-of-Stock
                </div>

                {isEditing ? (
                  <div className="flex items-center space-x-1">
                    <input
                      type="number"
                      value={newStockValue}
                      onChange={e => setNewStockValue(Number(e.target.value))}
                      className="w-16 px-2 py-1 bg-slate-900 border border-slate-700 rounded text-xs text-white font-mono"
                    />
                    <button
                      onClick={() => handleSaveStock(item.productId)}
                      className="px-2.5 py-1 text-xs bg-brand-cyan text-black font-semibold rounded hover:bg-cyan-300"
                    >
                      Save
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => {
                      setEditingStockId(item.productId);
                      setNewStockValue(item.availableQuantity);
                    }}
                    className="flex items-center space-x-1 px-2.5 py-1 text-xs font-medium text-slate-300 hover:text-white bg-slate-800/80 hover:bg-slate-800 rounded border border-slate-700 transition-all"
                  >
                    <Edit3 className="h-3 w-3" />
                    <span>Set Stock</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
