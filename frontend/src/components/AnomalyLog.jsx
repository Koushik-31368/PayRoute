import React from 'react';
import './AnomalyLog.css';

const TYPE_META = {
  LARGE_AMOUNT:      { label: 'Large Amount',      icon: '💰', color: 'var(--amber)' },
  BURST_FROM_SOURCE: { label: 'Burst Activity',    icon: '⚡', color: 'var(--red)' },
  REPEATED_FAILURES: { label: 'Repeated Failures', icon: '🔁', color: 'var(--red)' },
};

function formatTime(isoString) {
  return new Date(isoString).toLocaleTimeString('en-IN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false,
  });
}

function formatAmount(amount) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0,
  }).format(amount);
}

export default function AnomalyLog({ anomalies }) {
  if (anomalies.length === 0) {
    return (
      <div className="anomaly-empty">
        <span>🛡️</span>
        <p>No anomalies detected</p>
      </div>
    );
  }

  return (
    <div className="anomaly-list">
      {anomalies.slice(0, 100).map((a) => {
        const meta = TYPE_META[a.anomalyType || a.type] || { label: 'Unknown', icon: '⚠️', color: 'var(--amber)' };
        return (
          <div key={a.id} className="anomaly-item">
            <span className="anomaly-icon">{meta.icon}</span>
            <div className="anomaly-body">
              <div className="anomaly-header">
                <span className="anomaly-type" style={{ color: meta.color }}>
                  {meta.label}
                </span>
                <span className="anomaly-time">{formatTime(a.detectedAt)}</span>
              </div>
              <p className="anomaly-reason">{a.reason}</p>
              <div className="anomaly-meta">
                <span className="anomaly-source">{a.source}</span>
                <span className="anomaly-amount">{formatAmount(a.amount)}</span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
