import React from 'react';
import { formatINR, formatTime } from '../utils/format';
import './LiveTransactionFeed.css';

const STATUS_META = {
  SUCCESS: { color: 'var(--green)',  label: 'SUCCESS', bg: 'rgba(var(--green-rgb), 0.1)' },
  FAILED:  { color: 'var(--red)',    label: 'FAILED',  bg: 'rgba(var(--red-rgb), 0.1)'   },
  PENDING: { color: 'var(--amber)',  label: 'PENDING', bg: 'rgba(var(--amber-rgb), 0.1)' },
};

const RESULT_COLOR = {
  SUCCESS:  'var(--green)',
  FAILURE:  'var(--red)',
  TIMEOUT:  'var(--amber)',
  SKIPPED:  'var(--text-muted)',
};

// formatINR and formatTime imported from utils/format.js

/**
 * A single transaction row, showing:
 *   - Status badge, amount, source, time
 *   - Attempt chips (provider → result → latency)
 */
function TransactionRow({ txn, isNew }) {
  const meta = STATUS_META[txn.status] || STATUS_META.PENDING;

  return (
    <div className={`txn-row ${isNew ? 'txn-row--new' : ''}`}>
      <div className="txn-row__main">
        <div className="txn-row__left">
          <span className="txn-badge" style={{ color: meta.color, background: meta.bg }}>
            {meta.label}
          </span>
          <span className="txn-amount">{formatINR(txn.amount)}</span>
          <span className="txn-source">{txn.source}</span>
        </div>
        <div className="txn-row__right">
          {txn.finalProvider && (
            <span className="txn-provider">{txn.finalProvider}</span>
          )}
          <span className="txn-time">{formatTime(txn.createdAt)}</span>
        </div>
      </div>

      {txn.attempts && txn.attempts.length > 0 && (
        <div className="txn-attempts">
          {txn.attempts.map((a, i) => (
            <span
              key={i}
              className="txn-attempt-chip"
              style={{ borderColor: RESULT_COLOR[a.result] || 'var(--border)' }}
            >
              <span style={{ color: RESULT_COLOR[a.result] }}>
                {a.provider?.replace('PROVIDER_', 'P')}
              </span>
              <span className="txn-attempt-sep">·</span>
              <span style={{ color: RESULT_COLOR[a.result], fontSize: '10px' }}>
                {a.result}
              </span>
              {a.result !== 'SKIPPED' && (
                <>
                  <span className="txn-attempt-sep">·</span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '10px' }}>
                    {a.latencyMs}ms
                  </span>
                </>
              )}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

/**
 * Scrollable transaction feed — newest at top, max 200 items.
 */
export default function LiveTransactionFeed({ transactions }) {
  if (transactions.length === 0) {
    return (
      <div className="feed-empty">
        <span className="feed-empty__icon">⚡</span>
        <p>Waiting for transactions...</p>
        <p className="feed-empty__sub">Submit a payment or run a burst simulation</p>
      </div>
    );
  }

  return (
    <div className="txn-feed">
      {transactions.slice(0, 200).map((txn, idx) => (
        <TransactionRow key={txn.id} txn={txn} isNew={idx === 0} />
      ))}
    </div>
  );
}
