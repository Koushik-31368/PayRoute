/**
 * Centralised API layer â€” all fetch calls in one place.
 * In a larger app, you'd split this by feature. Here it stays lean.
 */

const BASE_URL = import.meta.env.VITE_API_URL || '';

async function handleResponse(res) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`HTTP ${res.status}: ${text}`);
  }
  return res.json();
}

/** Submit a payment transaction */
export async function submitTransaction({ amount, source, idempotencyKey }) {
  const res = await fetch(`${BASE_URL}/api/transactions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount, source, idempotencyKey }),
  });
  return handleResponse(res);
}

/** Get the 50 most recent transactions (initial load) */
export async function getTransactions() {
  const res = await fetch(`${BASE_URL}/api/transactions`);
  return handleResponse(res);
}

/** Get circuit breaker states for all providers */
export async function getCircuitBreakers() {
  const res = await fetch(`${BASE_URL}/api/circuit-breakers`);
  return handleResponse(res);
}

/** Get the anomaly log (most recent 100, newest first) */
export async function getAnomalies() {
  const res = await fetch(`${BASE_URL}/api/anomalies`);
  return handleResponse(res);
}

