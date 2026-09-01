# PayRoute API Reference

Base URL: `http://localhost:8080`

---

## Transactions

### POST /api/transactions
Submit a new payment transaction. Idempotent - duplicate keys return the original result.

Request Body:
  amount: number (in INR paise)
  source: string (payer identifier)
  idempotencyKey: string (optional, auto-generated if missing)

Response: Transaction object with status, attempts, finalProvider

### GET /api/transactions
Returns the 50 most recent transactions, newest first.

---

## Circuit Breakers

### GET /api/circuit-breakers
Returns current state of all provider circuit breakers.

States: CLOSED (healthy), OPEN (tripped), HALF_OPEN (probing)

---

## Anomalies

### GET /api/anomalies
Returns the 100 most recent anomaly flags.

Anomaly Types:
  LARGE_AMOUNT       - transaction above configured threshold (default: 100000)
  BURST_FROM_SOURCE  - same source exceeded burst limit in time window
  REPEATED_FAILURES  - same source accumulated too many failures

---

## WebSocket

Connect to /ws using STOMP over SockJS.

Topics:
  /topic/transactions - real-time transaction events
  /topic/anomalies    - real-time anomaly alerts
