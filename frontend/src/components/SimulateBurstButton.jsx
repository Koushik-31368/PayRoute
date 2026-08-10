import React, { useState } from 'react';
import { submitTransaction } from '../api/payroute';
import './SimulateBurstButton.css';

/**
 * "Simulate Burst" button — fires N transactions quickly from the same source.
 *
 * Purpose: lets you watch the circuit breaker trip live in a demo.
 * When you fire 20+ transactions rapidly, Provider C (40% failure rate)
 * will accumulate failures fast, trip its circuit breaker, and future
 * transactions will be routed away from it automatically.
 *
 * The source is fixed as "burst-demo" so anomaly detection also fires.
 */
export default function SimulateBurstButton() {
  const [isRunning, setIsRunning] = useState(false);
  const [progress, setProgress] = useState({ done: 0, total: 0 });
  const [count, setCount] = useState(20);
  const [amount, setAmount] = useState(1000);

  async function handleBurst() {
    setIsRunning(true);
    setProgress({ done: 0, total: count });

    const source = 'burst-demo';
    const promises = [];

    for (let i = 0; i < count; i++) {
      // Small stagger to avoid overwhelming the backend simultaneously,
      // but fast enough to trigger burst anomaly detection.
      await new Promise(r => setTimeout(r, 80));
      const p = submitTransaction({
        amount: amount,
        source,
        // Each request gets a unique key — we WANT these processed separately
        idempotencyKey: `burst-${Date.now()}-${i}`,
      }).catch(() => null); // Don't let one failure stop the burst
      promises.push(p);
      setProgress({ done: i + 1, total: count });
    }

    await Promise.allSettled(promises);
    setIsRunning(false);
    setProgress({ done: 0, total: 0 });
  }

  return (
    <div className="burst-panel">
      <div className="burst-controls">
        <label className="burst-label">
          <span>Transactions</span>
          <input
            type="number"
            min={5}
            max={100}
            value={count}
            onChange={e => setCount(Number(e.target.value))}
            className="burst-input"
            disabled={isRunning}
          />
        </label>
        <label className="burst-label">
          <span>Amount (₹)</span>
          <input
            type="number"
            min={1}
            max={9999999}
            value={amount}
            onChange={e => setAmount(Number(e.target.value))}
            className="burst-input"
            disabled={isRunning}
          />
        </label>
      </div>

      <button
        className={`burst-btn ${isRunning ? 'burst-btn--running' : ''}`}
        onClick={handleBurst}
        disabled={isRunning}
      >
        {isRunning ? (
          <>
            <span className="burst-spinner" />
            Firing {progress.done}/{progress.total}...
          </>
        ) : (
          <>⚡ Simulate Burst</>
        )}
      </button>

      {isRunning && (
        <div className="burst-progress">
          <div
            className="burst-progress__bar"
            style={{ width: `${(progress.done / progress.total) * 100}%` }}
          />
        </div>
      )}
    </div>
  );
}
