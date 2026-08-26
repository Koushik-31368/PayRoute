import React from 'react';
import './ProviderHealthCard.css';

const STATE_META = {
  CLOSED:    { label: 'Healthy',   color: 'var(--green)',  dot: '●', pulse: false },
  HALF_OPEN: { label: 'Probing',   color: 'var(--amber)',  dot: '●', pulse: true  },
  OPEN:      { label: 'Open',      color: 'var(--red)',    dot: '●', pulse: false },
};

const PROVIDER_LABELS = {
  PROVIDER_A: { name: 'Provider A', subtitle: 'Low Risk · ~125ms' },
  PROVIDER_B: { name: 'Provider B', subtitle: 'Med Risk · ~250ms' },
  PROVIDER_C: { name: 'Provider C', subtitle: 'High Risk · ~500ms' },
};

/**
 * Displays the health of a single provider:
 *   - Circuit breaker state (color-coded)
 *   - Failure rate (from rolling window)
 *   - Success rate bar
 *   - Average latency
 */
export default function ProviderHealthCard({ provider, state, failureRate, avgLatency, windowSamples }) {
  const meta = STATE_META[state] || STATE_META.CLOSED;
  const label = PROVIDER_LABELS[provider] || { name: provider, subtitle: '' };
  const successRate = Math.max(0, 100 - failureRate).toFixed(1);
  const isUnhealthy = state === 'OPEN' || state === 'HALF_OPEN';

  return (
    <div className={`health-card health-card--${state.toLowerCase()}`}>
      <div className="health-card__header">
        <div className="health-card__title">
          <span className="health-card__name">{label.name}</span>
          <span className="health-card__subtitle">{label.subtitle}</span>
        </div>
        <div className="health-card__state" style={{ color: meta.color }}>
          <span className={`health-card__dot ${meta.pulse ? 'pulse' : ''}`} style={{ color: meta.color }}>
            {meta.dot}
          </span>
          <span className="health-card__state-label">{meta.label}</span>
        </div>
      </div>

      <div
        className="health-card__bar-wrapper"
        title={`Success rate: ${successRate}% over last ${windowSamples} samples`}
      >
        <div
          className="health-card__bar"
          style={{
            width: `${successRate}%`,
            background: meta.color,
          }}
        />
      </div>

      {isUnhealthy && (
        <div className="health-card__alert">
          ⚠ Failure rate: {failureRate.toFixed(1)}%
        </div>
      )}

      <div className="health-card__stats">
        <div className="health-card__stat">
          <span className="health-card__stat-value" style={{ color: meta.color }}>
            {successRate}%
          </span>
          <span className="health-card__stat-label">Success Rate</span>
        </div>
        <div className="health-card__stat">
          <span className="health-card__stat-value">
            {avgLatency ? `${Math.round(avgLatency)}ms` : '—'}
          </span>
          <span className="health-card__stat-label">Avg Latency</span>
        </div>
        <div className="health-card__stat">
          <span className="health-card__stat-value">{windowSamples}</span>
          <span className="health-card__stat-label">Samples</span>
        </div>
      </div>
    </div>
  );
}
