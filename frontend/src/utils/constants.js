/**
 * Application-wide constants for PayRoute frontend.
 * Import from here instead of hardcoding strings across components.
 */

/** WebSocket topics */
export const WS_TOPICS = {
  TRANSACTIONS: '/topic/transactions',
  ANOMALIES:    '/topic/anomalies',
};

/** Transaction status values */
export const TX_STATUS = {
  SUCCESS: 'SUCCESS',
  FAILED:  'FAILED',
  PENDING: 'PENDING',
};

/** Circuit breaker state values */
export const CB_STATE = {
  CLOSED:    'CLOSED',
  OPEN:      'OPEN',
  HALF_OPEN: 'HALF_OPEN',
};

/** Anomaly type values */
export const ANOMALY_TYPE = {
  LARGE_AMOUNT:      'LARGE_AMOUNT',
  BURST_FROM_SOURCE: 'BURST_FROM_SOURCE',
  REPEATED_FAILURES: 'REPEATED_FAILURES',
};

/** Max items to display in feeds */
export const FEED_MAX_ITEMS    = 200;
export const ANOMALY_MAX_ITEMS = 100;
