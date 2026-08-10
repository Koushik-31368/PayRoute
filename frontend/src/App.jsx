import React, { useState, useEffect, useCallback, useRef } from 'react';
import './index.css';
import ProviderHealthCard from './components/ProviderHealthCard';
import LiveTransactionFeed from './components/LiveTransactionFeed';
import AnomalyLog from './components/AnomalyLog';
import SimulateBurstButton from './components/SimulateBurstButton';
import { useWebSocket } from './hooks/useWebSocket';
import {
  getTransactions,
  getCircuitBreakers,
  getAnomalies,
  submitTransaction,
} from './api/payroute';
import { v4 as uuid } from 'uuid';

export default function App() {
  // ── State ──────────────────────────────────────────────────
  const [transactions, setTransactions] = useState([]);
  const [providers, setProviders] = useState([]);
  const [anomalies, setAnomalies] = useState([]);
  const [wsConnected, setWsConnected] = useState(false);

  // Manual submit form
  const [formAmount, setFormAmount] = useState('5000');
  const [formSource, setFormSource] = useState('customer-001');
  const [submitting, setSubmitting] = useState(false);

  // Stats
  const [stats, setStats] = useState({ total: 0, success: 0, failed: 0 });

  // ── Initial data load ──────────────────────────────────────
  useEffect(() => {
    async function load() {
      try {
        const [txns, cbs, anoms] = await Promise.all([
          getTransactions(),
          getCircuitBreakers(),
          getAnomalies(),
        ]);
        setTransactions(txns);
        setProviders(cbs);
        setAnomalies(anoms);
        computeStats(txns, { total: 0, success: 0, failed: 0 });
      } catch (e) {
        console.error('Failed to load initial data:', e);
      }
    }
    load();
  }, []);

  function computeStats(txns, prev) {
    const total = txns.length;
    const success = txns.filter(t => t.status === 'SUCCESS').length;
    const failed = txns.filter(t => t.status === 'FAILED').length;
    setStats({ total, success, failed });
  }

  // ── Poll circuit breaker state every 3s ────────────────────
  useEffect(() => {
    const id = setInterval(async () => {
      try {
        const cbs = await getCircuitBreakers();
        setProviders(cbs);
      } catch (_) {}
    }, 3000);
    return () => clearInterval(id);
  }, []);

  // ── WebSocket for live updates ──────────────────────────────
  const handleWsMessage = useCallback((topic, body) => {
    setWsConnected(true);
    if (topic === '/topic/transactions') {
      setTransactions(prev => {
        const updated = [body, ...prev.filter(t => t.id !== body.id)];
        computeStats(updated, stats);
        return updated;
      });
    } else if (topic === '/topic/anomalies') {
      setAnomalies(prev => [body, ...prev.filter(a => a.id !== body.id)]);
    }
  }, []);

  useWebSocket({
    topics: ['/topic/transactions', '/topic/anomalies'],
    onMessage: handleWsMessage,
  });

  // ── Manual submit ──────────────────────────────────────────
  async function handleSubmit(e) {
    e.preventDefault();
    if (!formAmount || !formSource) return;
    setSubmitting(true);
    try {
      await submitTransaction({
        amount: parseFloat(formAmount),
        source: formSource.trim(),
        idempotencyKey: uuid(),
      });
    } catch (err) {
      console.error('Submit failed:', err);
    } finally {
      setSubmitting(false);
    }
  }

  // Build provider stats lookup
  const providerLatencyMap = {};

  return (
    <div className="app">
      {/* ── Header ── */}
      <header className="header">
        <div className="header__logo">
          <div className="header__logo-mark">PR</div>
          <div>
            <div className="header__name">PayRoute</div>
            <div className="header__tagline">Payment Orchestration Engine</div>
          </div>
        </div>
        <div className="header__status">
          <div className={`header__status-dot ${wsConnected ? '' : 'header__status-dot--disconnected'}`} />
          {wsConnected ? 'Live' : 'Connecting...'}
        </div>
      </header>

      <main className="main">
        {/* ── Stats Bar ── */}
        <div className="stats-row">
          <div className="stat-chip">
            <span className="stat-chip__value">{stats.total}</span>
            <span className="stat-chip__label">Total</span>
          </div>
          <div className="stat-chip">
            <span className="stat-chip__value" style={{ color: 'var(--green)' }}>{stats.success}</span>
            <span className="stat-chip__label">Success</span>
          </div>
          <div className="stat-chip">
            <span className="stat-chip__value" style={{ color: 'var(--red)' }}>{stats.failed}</span>
            <span className="stat-chip__label">Failed</span>
          </div>
          <div className="stat-chip">
            <span className="stat-chip__value" style={{ color: 'var(--amber)' }}>{anomalies.length}</span>
            <span className="stat-chip__label">Anomalies</span>
          </div>
          <div className="stat-chip">
            <span className="stat-chip__value">
              {stats.total > 0 ? ((stats.success / stats.total) * 100).toFixed(1) : '—'}%
            </span>
            <span className="stat-chip__label">Success Rate</span>
          </div>
        </div>

        {/* ── Provider Health ── */}
        <section className="section">
          <div className="section__header">
            <h2 className="section__title">⚡ Provider Health</h2>
            <span className="section__badge">Circuit Breakers</span>
          </div>
          <div className="section__body">
            <div className="provider-grid">
              {providers.map(p => (
                <ProviderHealthCard
                  key={p.provider}
                  provider={p.provider}
                  state={p.state}
                  failureRate={p.failureRate}
                  windowSamples={p.windowSamples}
                  avgLatency={providerLatencyMap[p.provider]}
                />
              ))}
            </div>
          </div>
        </section>

        {/* ── Submit + Burst ── */}
        <section className="section">
          <div className="section__header">
            <h2 className="section__title">💳 Submit Payment</h2>
          </div>
          <div className="section__body">
            <form className="submit-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Amount (₹)</label>
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={formAmount}
                  onChange={e => setFormAmount(e.target.value)}
                  placeholder="5000"
                  disabled={submitting}
                />
              </div>
              <div className="form-group">
                <label>Source / Customer ID</label>
                <input
                  type="text"
                  value={formSource}
                  onChange={e => setFormSource(e.target.value)}
                  placeholder="customer-001"
                  disabled={submitting}
                />
              </div>
              <button type="submit" className="submit-btn" disabled={submitting}>
                {submitting ? 'Routing...' : 'Pay'}
              </button>
            </form>
          </div>
        </section>

        {/* ── Burst Simulator ── */}
        <section className="section">
          <div className="section__header">
            <h2 className="section__title">🔥 Burst Simulator</h2>
            <span className="section__badge">Trips Circuit Breakers</span>
          </div>
          <div className="section__body">
            <SimulateBurstButton />
          </div>
        </section>

        {/* ── Live Feed + Anomalies ── */}
        <div className="two-col">
          <section className="section">
            <div className="section__header">
              <h2 className="section__title">📡 Live Transaction Feed</h2>
              <span className="section__badge">{transactions.length} transactions</span>
            </div>
            <div className="section__body">
              <LiveTransactionFeed transactions={transactions} />
            </div>
          </section>

          <section className="section">
            <div className="section__header">
              <h2 className="section__title">🚨 Anomaly Log</h2>
              <span className="section__badge">{anomalies.length}</span>
            </div>
            <div className="section__body">
              <AnomalyLog anomalies={anomalies} />
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
