# PayRoute
<!-- Last Updated: 2026-09-18 --> â€” Intelligent Payment Orchestration Engine

![Tech: Spring Boot](https://img.shields.io/badge/backend-Spring%20Boot%203.2-6db33f?logo=springboot&logoColor=white)
![Tech: React](https://img.shields.io/badge/frontend-React%2018-61dafb?logo=react&logoColor=black)
![Tech: PostgreSQL](https://img.shields.io/badge/database-PostgreSQL%2015-4169e1?logo=postgresql&logoColor=white)
![Tech: Docker](https://img.shields.io/badge/containerised-Docker%20Compose-2496ed?logo=docker&logoColor=white)

> A full-stack simulation of how real payment aggregators (Stripe, Razorpay, Juspay) route transactions across multiple providers with automatic failover, circuit breaking, and anomaly detection.

**This is a portfolio/learning project â€” no real money, no real PCI concerns. Everything is simulated.**

---

## Architecture

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                        PAYROUTE SYSTEM                               â”‚
â”‚                                                                      â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    â”‚
â”‚  â”‚                   React Frontend (Nginx)                     â”‚    â”‚
â”‚  â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â” â”‚    â”‚
â”‚  â”‚  â”‚Provider Cardsâ”‚ â”‚ Live Feed  â”‚ â”‚  Burst   â”‚ â”‚Anomaly  â”‚ â”‚    â”‚
â”‚  â”‚  â”‚(CB states)   â”‚ â”‚(WebSocket) â”‚ â”‚ Sim Btn  â”‚ â”‚  Log    â”‚ â”‚    â”‚
â”‚  â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â”‚    â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜    â”‚
â”‚                           â”‚ REST + WebSocket (STOMP/SockJS)         â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    â”‚
â”‚  â”‚                Spring Boot Backend                           â”‚    â”‚
â”‚  â”‚                                                              â”‚    â”‚
â”‚  â”‚  POST /api/transactions                                      â”‚    â”‚
â”‚  â”‚         â”‚                                                    â”‚    â”‚
â”‚  â”‚         â–¼                                                    â”‚    â”‚
â”‚  â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚    â”‚
â”‚  â”‚  â”‚ Idempotency     â”‚    â”‚    Circuit Breaker Registry   â”‚   â”‚    â”‚
â”‚  â”‚  â”‚ Check (DB)      â”‚    â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â” â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”        â”‚   â”‚    â”‚
â”‚  â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”˜    â”‚  â”‚  CB_A  â”‚ â”‚  CB_B  â”‚  CB_C  â”‚   â”‚    â”‚
â”‚  â”‚           â”‚             â”‚  â”‚CLOSED  â”‚ â”‚  OPEN  â”‚ HALF   â”‚   â”‚    â”‚
â”‚  â”‚           â–¼             â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”˜ â””â”€â”€â”€â”€â”€â”€â”€â”€â”˜        â”‚   â”‚    â”‚
â”‚  â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â” â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚    â”‚
â”‚  â”‚  â”‚  Routing Engine    â”‚                                     â”‚    â”‚
â”‚  â”‚  â”‚  (success-rate     â”‚â”€â”€â”€â”€ allows? â”€â”€â–º  Provider A         â”‚    â”‚
â”‚  â”‚  â”‚   ordered)         â”‚â”€â”€â”€â”€ skipped â”€â”€â–º  Provider B (OPEN)  â”‚    â”‚
â”‚  â”‚  â”‚                    â”‚â”€â”€â”€â”€ allows? â”€â”€â–º  Provider C          â”‚    â”‚
â”‚  â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜                                     â”‚    â”‚
â”‚  â”‚           â”‚                                                  â”‚    â”‚
â”‚  â”‚           â–¼                                                  â”‚    â”‚
â”‚  â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”         â”‚    â”‚
â”‚  â”‚  â”‚ Anomaly Detection  â”‚   â”‚  WebSocket Publisher  â”‚         â”‚    â”‚
â”‚  â”‚  â”‚ (3 rule checks)    â”‚   â”‚ /topic/transactions   â”‚         â”‚    â”‚
â”‚  â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚ /topic/anomalies      â”‚         â”‚    â”‚
â”‚  â”‚                           â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜         â”‚    â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜    â”‚
â”‚                           â”‚                                          â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    â”‚
â”‚  â”‚              PostgreSQL 15                                   â”‚    â”‚
â”‚  â”‚   transactions | transaction_attempts | anomalies            â”‚    â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜    â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
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

## UI Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + Enter` (or `âŒ˜ + Enter` on Mac) | Submit payment from anywhere on the page |

Form values (amount and customer ID) are automatically persisted in `localStorage` across page reloads.

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

### 1. Circuit Breaker â€” Manual Implementation

The circuit breaker (`CircuitBreaker.java`) is implemented from scratch using a rolling window Deque rather than a library like Resilience4j. Here's why each piece works the way it does:

#### States and Transitions

```
CLOSED â”€â”€[failure rate > 50% in last 20 attempts]â”€â”€â–º OPEN
  â–²                                                      â”‚
  â”‚                                               [30s cooldown]
  â”‚                                                      â”‚
  â””â”€â”€[probe SUCCEEDS]â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ HALF_OPEN â—„â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                                     â”‚
                          [probe FAILS]â”€â”€â–º OPEN (cooldown resets)
```

**CLOSED** â€” Normal state. Every request goes through. The breaker tracks results in a sliding window.

**OPEN** â€” Provider is sick. All requests are rejected immediately (recorded as `SKIPPED`) without a real network call. This prevents cascading failures: instead of waiting 800ms for Provider C to timeout, the router skips it instantly and tries the next provider.

**HALF_OPEN** â€” The cooldown has expired. We allow exactly ONE "probe" request through. If it succeeds, we trust the provider again (â†’ CLOSED). If it fails, we reset the cooldown (â†’ OPEN). Without HALF_OPEN, you'd either stay OPEN forever or snap back to full traffic on a still-sick provider.

#### Why these threshold defaults?

| Setting | Default | Reasoning |
|---------|---------|-----------|
| `window-size` | 20 | Small enough to react to spikes (responds within 20 transactions), large enough to avoid false trips from 1-2 random failures |
| `failure-threshold` | 50% | At 50% failure rate the provider is clearly degraded. Lower (30%) = more sensitive, more false trips. Higher (70%) = slower to open, more failed transactions before protection kicks in |
| `cooldown-seconds` | 30 | Gives the provider ~30s to recover. For slow-recovering services (DB restarts), increase to 60-120s |

#### Thread Safety

The breaker uses a `ReentrantLock` to protect state transitions. The critical section is in `allowRequest()` during HALF_OPEN: the lock ensures that only ONE thread sets `probeInFlight = true` and gets through, even under high concurrency. Without the lock, two concurrent requests could both read `probeInFlight = false` simultaneously and both think they're the probe.

### 2. Routing Strategy â€” Success Rate Ordered

Providers are tried in order of ascending failure rate (best first). This is better than round-robin because:
- Round-robin sends 33% of traffic to Provider C even when it's failing 40% of the time
- Success-rate ordering continuously self-optimises â€” as a provider's window fills with failures, it drops to the bottom of the list
- Combined with circuit breakers, a consistently failing provider eventually gets cut out entirely

### 3. Idempotency â€” DB-Level Safety Net

The idempotency key has a UNIQUE constraint in the database. The flow:
1. Check `findByIdempotencyKey(key)` â€” if found, return existing result
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

**Same app (chosen):** âœ“ One docker-compose service, simpler networking, faster startup. âœ— Failures can't crash independently of the orchestrator.

**Separate services (production approach):** âœ“ More realistic â€” network timeouts are actual TCP timeouts. You can kill/restart provider containers independently. âœ— Three extra services in docker-compose, harder to manage in a demo.

For a portfolio project where the learning goal is the orchestration logic, same-app simulation provides the same educational value with far less friction. In production, you'd point the simulator at actual HTTP endpoints (or use Wiremock stubs).

### 6. Anomaly Detection â€” REQUIRES_NEW Transaction

The `AnomalyDetectionService` runs in its own Spring transaction (`Propagation.REQUIRES_NEW`). This means:
- If anomaly detection throws a database exception, it does NOT roll back the main payment record
- The payment is always recorded, regardless of what happens in anomaly detection
- This is the correct pattern for "best effort" side effects that shouldn't block the main flow

---

## Project Structure

```
PayRoute/
â”œâ”€â”€ backend/
â”‚   â”œâ”€â”€ src/main/java/com/payroute/
â”‚   â”‚   â”œâ”€â”€ config/          # CORS + WebSocket config
â”‚   â”‚   â”œâ”€â”€ transaction/     # Domain: entities, service, controller
â”‚   â”‚   â”œâ”€â”€ circuitbreaker/  # State machine implementation
â”‚   â”‚   â”œâ”€â”€ anomaly/         # Detection rules + storage
â”‚   â”‚   â”œâ”€â”€ provider/        # Mock provider simulation
â”‚   â”‚   â””â”€â”€ websocket/       # Real-time event publisher
â”‚   â”œâ”€â”€ Dockerfile
â”‚   â””â”€â”€ pom.xml
â”œâ”€â”€ frontend/
â”‚   â”œâ”€â”€ src/
â”‚   â”‚   â”œâ”€â”€ api/             # All fetch calls in one place
â”‚   â”‚   â”œâ”€â”€ hooks/           # useWebSocket (reusable)
â”‚   â”‚   â””â”€â”€ components/      # UI components
â”‚   â”œâ”€â”€ Dockerfile
â”‚   â””â”€â”€ nginx.conf
â””â”€â”€ docker-compose.yml
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

