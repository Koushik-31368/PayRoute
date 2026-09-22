/**
 * @module format
 * Shared formatting utilities used across multiple components.
 *
 * Centralising these prevents drift where LiveTransactionFeed and AnomalyLog
 * each define their own formatAmount with slightly different options.
 */

/**
 * Format a number as Indian Rupee currency.
 * @param {number} amount
 * @example
 * // returns '₹5,000.00'
 * formatINR(5000)
 * @param {boolean} [compact=false] - Use compact notation for large amounts (e.g. ₹11.2L)
 */
export function formatINR(amount, compact = false) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
    ...(compact && { notation: 'compact', compactDisplay: 'short' }),
  }).format(amount);
}

/**
 * Format an ISO date string to a short time (HH:MM:SS).
 * @param {string} isoString
 */
export function formatTime(isoString) {
  return new Date(isoString).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

/**
 * Format an ISO date string to a full date + time.
 * @param {string} isoString
 */
export function formatDateTime(isoString) {
  return new Date(isoString).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

/**
 * Returns a human-readable relative time string (e.g. "2s ago", "5m ago").
 * @param {string} isoString
 * @example
 * // returns '2s ago'
 * timeAgo(new Date(Date.now() - 2000).toISOString())
 */
export function timeAgo(isoString) {
  const diffMs = Date.now() - new Date(isoString).getTime();
  const diffSec = Math.floor(diffMs / 1000);
  if (diffSec < 60) return `${diffSec}s ago`;
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  return `${diffHr}h ago`;
}

/**
 * Format a latency value in milliseconds to a human-readable string.
 * Examples: 450 -> "450ms", 1200 -> "1.2s"
 * @param {number} ms
 * @returns {string}
 */
export function formatLatency(ms) {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}



