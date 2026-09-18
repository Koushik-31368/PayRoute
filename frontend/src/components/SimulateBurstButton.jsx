import React, { useState } from 'react';
import { submitTransaction } from '../api/payroute';
import { BURST_STAGGER_MS, BURST_SOURCE } from '../utils/constants';
import './SimulateBurstButton.css';

/** Default number of transactions to fire in a burst */
const DEFAULT_BURST_COUNT = 20;

/** Default amount per transaction in the burst (INR) */
const DEFAULT_BURST_AMOUNT = 1000;

/**
 * "Simulate Burst" button – fires N transactions quickly from the same source.
 *
 * Purpose: lets you watch the circuit breaker trip live in a demo.
 * When you fire 20+ transactions rapidly, Provider C (40% failure rate)
 * will accumulate failures fast, trip its circuit breaker, and future
 * transactions will be routed away from it automatically.
 *
 * The source is fixed as BURST_SOURCE so anomaly detection also fires.
 */
export default function SimulateBurstButton() {
  const [isRunning, setIsRunning] = useState(false);
  const [progress, setProgress] = useState({ done: 0, total: 0 });
  const [count, setCount] = useState(DEFAULT_BURST_COUNT);
  const [amount, setAmount] = useState(DEFAULT_BURST_AMOUNT);

  async function handleBurst() {
    setIsRunning(true);
    setProgress({ done: 0, total: count });

    const promises = [];

    for (let i = 0; i < count; i++) {
      // Small stagger to avoid overwhelming the backend simultaneously,
      // but fast enough to trigger burst anomaly detection.
      await new Promise(r => setTimeout(r, BURST_STAGGER_MS));
      const p = submitTransaction({
        amount,
        source: BURST_SOURCE,
        // Each request gets a unique key – we WANT these processed separately
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
            id="burst-count"
            type="number"
            min={5}
            max={100}
            value={count}
            onChange={e => setCount(Number(e.target.value))}
            className="burst-input"
            disabled={isRunning}
            aria-label="Number of transactions to fire in burst"
          />
        </label>
        <label className="burst-label">
          <span>Amount (₹)</span>
          <input
            id="burst-amount"
            type="number"
            min={1}
            max={9999999}
            value={amount}
            onChange={e => setAmount(Number(e.target.value))}
            className="burst-input"
            disabled={isRunning}
            aria-label="Amount per transaction in burst"
          />
        </label>
      </div>

      <button
        id="burst-submit-btn"
        className={`burst-btn ${isRunning ? 'burst-btn--running' : ''}`}
        onClick={handleBurst}
        disabled={isRunning}
        aria-label={isRunning ? `Firing burst: ${progress.done} of ${progress.total}` : 'Simulate burst of transactions'}
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
        <div className="burst-progress" role="progressbar" aria-valuenow={progress.done} aria-valuemax={progress.total}>
          <div
            className="burst-progress__bar"
            style={{ width: `${(progress.done / progress.total) * 100}%` }}
          />
        </div>
      )}
    </div>
  );
}
