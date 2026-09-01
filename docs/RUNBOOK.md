# PayRoute RUNBOOK - Local Dev Quick-Start

## Prerequisites

- Java 17+, Maven
- Node.js 18+, npm
- Docker + Docker Compose

## Start Everything (Docker)

```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Start backend
cd backend && mvn spring-boot:run

# 3. Start frontend
cd frontend && npm install && npm run dev
```

Frontend: http://localhost:5173
Backend: http://localhost:8080

## Demo Sequence

### 1. Normal Operation
- Submit a payment with amount=5000, source=demo-user
- Watch it appear in the Live Feed (should be SUCCESS via PROVIDER_A)

### 2. Trip a Circuit Breaker
- Click "Simulate Burst" with count=30, amount=1000
- Watch PROVIDER_C circuit breaker flip OPEN in the health cards
- Future transactions skip PROVIDER_C automatically

### 3. Trigger Anomaly Detection
- Submit amount=200000 (above 100000 threshold)
  → LARGE_AMOUNT anomaly fires
- Submit 6+ transactions with same source within 60 seconds
  → BURST_FROM_SOURCE anomaly fires

### 4. Watch Circuit Recovery
- Wait 30 seconds (cooldown-seconds default)
- PROVIDER_C moves to HALF_OPEN, then probes, then CLOSED

## Verify Health

```bash
curl http://localhost:8080/api/circuit-breakers
curl http://localhost:8080/api/anomalies
curl http://localhost:8080/api/transactions
```
