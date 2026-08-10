# PayRoute — Intelligent Payment Orchestration Engine

> A full-stack simulation of how real payment aggregators (Stripe, Razorpay, Juspay) route transactions across multiple providers with automatic failover, circuit breaking, and anomaly detection.

**This is a portfolio/learning project — no real money, no real PCI concerns. Everything is simulated.**

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                        PAYROUTE SYSTEM                               │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                   React Frontend (Nginx)                     │    │
│  │  ┌──────────────┐ ┌────────────┐ ┌──────────┐ ┌─────────┐ │    │
│  │  │Provider Cards│ │ Live Feed  │ │  Burst   │ │Anomaly  │ │    │
│  │  │(CB states)   │ │(WebSocket) │ │ Sim Btn  │ │  Log    │ │    │
│  │  └──────────────┘ └────────────┘ └──────────┘ └─────────┘ │    │
│  └────────────────────────┬────────────────────────────────────┘    │
│                           │ REST + WebSocket (STOMP/SockJS)         │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │                Spring Boot Backend                           │    │
│  │                                                              │    │
│  │  POST /api/transactions                                      │    │
│  │         │                                                    │    │
│  │         ▼                                                    │    │
│  │  ┌─────────────────┐    ┌──────────────────────────────┐   │    │
│  │  │ Idempotency     │    │    Circuit Breaker Registry   │   │    │
│  │  │ Check (DB)      │    │  ┌────────┐ ┌────────┐        │   │    │
│  │  └────────┬────────┘    │  │  CB_A  │ │  CB_B  │  CB_C  │   │    │
│  │           │             │  │CLOSED  │ │  OPEN  │ HALF   │   │    │
│  │           ▼             │  └────────┘ └────────┘        │   │    │
│  │  ┌────────────────────┐ └──────────────────────────────┘   │    │
│  │  │  Routing Engine    │                                     │    │
│  │  │  (success-rate     │──── allows? ──►  Provider A         │    │
│  │  │   ordered)         │──── skipped ──►  Provider B (OPEN)  │    │
│  │  │                    │──── allows? ──►  Provider C          │    │
│  │  └────────────────────┘                                     │    │
│  │           │                                                  │    │
│  │           ▼                                                  │    │
│  │  ┌────────────────────┐   ┌──────────────────────┐         │    │
│  │  │ Anomaly Detection  │   │  WebSocket Publisher  │         │    │
│  │  │ (3 rule checks)    │   │ /topic/transactions   │         │    │
│  │  └────────────────────┘   │ /topic/anomalies      │         │    │
│  │                           └──────────────────────┘         │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────┐    │
│  │              PostgreSQL 15                                   │    │
│  │   transactions | transaction_attempts | anomalies            │    │
│  └──────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites
- Docker + Docker Compose
- (For local dev) Java 17+, Maven 3.9+, Node 20+

### Run with Docker (recommended)

```bash
git clone https://github.com/Koushik-31368/PayRoute.git
cd PayRoute
docker-compose up --build
```

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (user: `payroute`, pass: `payroute`, db: `payroute`)

### Run locally (dev mode)

**Backend:**
```bash
# Start PostgreSQL first (or adjust application.properties datasource URL)
cd backend
mvn spring-boot:run
# Backend starts on http://localhost:8080
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# Dev server starts on http://localhost:5173 (proxies /api and /ws to :8080)
```

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions` | Submit a payment |
| `GET` | `/api/transactions` | List 50 most recent transactions |
| `GET` | `/api/circuit-breakers` | Provider health + circuit state |
| `GET` | `/api/anomalies` | 100 most recent anomaly flags |
| `WS` | `/ws` (STOMP) | Live event stream |

**Submit a transaction:**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "source": "customer-001",
    "idempotencyKey": "unique-key-123"
  }'
```

---

## Key Design Decisions

### 1. Circuit Breaker — Manual Implementation

The circuit breaker (`CircuitBreaker.java`) is implemented from scratch using a rolling window Deque rather than a library like Resilience4j. Here's why each piece works the way it does:

#### States and Transitions

```
CLOSED ──[failure rate > 50% in last 20 attempts]──► OPEN
  ▲                                                      │
  │                                               [30s cooldown]
  │                                                      │
  └──[probe SUCCEEDS]────────── HALF_OPEN ◄──────────────┘
                                     │
                          [probe FAILS]──► OPEN (cooldown resets)
```

**CLOSED** — Normal state. Every request goes through. The breaker tracks results in a sliding window.

**OPEN** — Provider is sick. All requests are rejected immediately (recorded as `SKIPPED`) without a real network call. This prevents cascading failures: instead of waiting 800ms for Provider C to timeout, the router skips it instantly and tries the next provider.

**HALF_OPEN** — The cooldown has expired. We allow exactly ONE "probe" request through. If it succeeds, we trust the provider again (→ CLOSED). If it fails, we reset the cooldown (→ OPEN). Without HALF_OPEN, you'd either stay OPEN forever or snap back to full traffic on a still-sick provider.

#### Why these threshold defaults?

| Setting | Default | Reasoning |
|---------|---------|-----------|
| `window-size` | 20 | Small enough to react to spikes (responds within 20 transactions), large enough to avoid false trips from 1-2 random failures |
| `failure-threshold` | 50% | At 50% failure rate the provider is clearly degraded. Lower (30%) = more sensitive, more false trips. Higher (70%) = slower to open, more failed transactions before protection kicks in |
| `cooldown-seconds` | 30 | Gives the provider ~30s to recover. For slow-recovering services (DB restarts), increase to 60-120s |

#### Thread Safety

The breaker uses a `ReentrantLock` to protect state transitions. The critical section is in `allowRequest()` during HALF_OPEN: the lock ensures that only ONE thread sets `probeInFlight = true` and gets through, even under high concurrency. Without the lock, two concurrent requests could both read `probeInFlight = false` simultaneously and both think they're the probe.

### 2. Routing Strategy — Success Rate Ordered

Providers are tried in order of ascending failure rate (best first). This is better than round-robin because:
- Round-robin sends 33% of traffic to Provider C even when it's failing 40% of the time
- Success-rate ordering continuously self-optimises — as a provider's window fills with failures, it drops to the bottom of the list
- Combined with circuit breakers, a consistently failing provider eventually gets cut out entirely

### 3. Idempotency — DB-Level Safety Net

The idempotency key has a UNIQUE constraint in the database. The flow:
1. Check `findByIdempotencyKey(key)` — if found, return existing result
2. If not found, insert the new Transaction

The subtle race condition: two concurrent requests with the same key could both pass step 1 (both see null) before either inserts. The DB UNIQUE constraint then makes one of them throw a `DataIntegrityViolationException`. In production, you'd add a distributed lock (Redis SETNX) or DB advisory lock for full correctness. This is documented in `TransactionService.java`.

### 4. Two Entities: Transaction + TransactionAttempt

A single `Transaction` (the user-facing payment) can have many `TransactionAttempt`s (one per provider tried). This separation gives you:
- Full audit trail per payment
- Per-provider success/failure counts for the circuit breaker rolling window  
- Average latency statistics per provider
- The ability to ask "show me every provider call for transaction XYZ"

### 5. Mock Providers: Same App vs. Separate Services

Mock providers live in the same Spring Boot app as the orchestrator (not separate Docker services). The tradeoff:

**Same app (chosen):** ✓ One docker-compose service, simpler networking, faster startup. ✗ Failures can't crash independently of the orchestrator.

**Separate services (production approach):** ✓ More realistic — network timeouts are actual TCP timeouts. You can kill/restart provider containers independently. ✗ Three extra services in docker-compose, harder to manage in a demo.

For a portfolio project where the learning goal is the orchestration logic, same-app simulation provides the same educational value with far less friction. In production, you'd point the simulator at actual HTTP endpoints (or use Wiremock stubs).

### 6. Anomaly Detection — REQUIRES_NEW Transaction

The `AnomalyDetectionService` runs in its own Spring transaction (`Propagation.REQUIRES_NEW`). This means:
- If anomaly detection throws a database exception, it does NOT roll back the main payment record
- The payment is always recorded, regardless of what happens in anomaly detection
- This is the correct pattern for "best effort" side effects that shouldn't block the main flow

---

## Project Structure

```
PayRoute/
├── backend/
│   ├── src/main/java/com/payroute/
│   │   ├── config/          # CORS + WebSocket config
│   │   ├── transaction/     # Domain: entities, service, controller
│   │   ├── circuitbreaker/  # State machine implementation
│   │   ├── anomaly/         # Detection rules + storage
│   │   ├── provider/        # Mock provider simulation
│   │   └── websocket/       # Real-time event publisher
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/             # All fetch calls in one place
│   │   ├── hooks/           # useWebSocket (reusable)
│   │   └── components/      # UI components
│   ├── Dockerfile
│   └── nginx.conf
└── docker-compose.yml
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java 17 |
| Real-time | STOMP over WebSocket (SockJS) |
| Database | PostgreSQL 15 |
| Frontend | React 18, Vite 5 |
| Containerization | Docker, Docker Compose |
| Styling | Vanilla CSS (design tokens) |

---

## Extending PayRoute

- **Add a provider**: Add to `payroute.providers` in `application.properties`, add config block, update `ProviderSimulator.getConfig()`
- **Add an anomaly rule**: Add an `AnomalyType` enum value, add detection logic in `AnomalyDetectionService.checkAndFlag()`
- **Replace the simple broker**: Swap `enableSimpleBroker` in `WebSocketConfig` for a full STOMP broker (RabbitMQ) for horizontal scaling
- **Add authentication**: Add Spring Security with JWT; the CORS config in `AppConfig` would need to reference the `SecurityFilterChain`
