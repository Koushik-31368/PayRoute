# PayRoute Architecture Decision Records

## ADR-001: Custom circuit breaker instead of Resilience4j

**Status:** Accepted

**Context:** The project needed a circuit breaker for payment provider routing. Resilience4j is the industry standard for Spring Boot.

**Decision:** Implement a custom CircuitBreaker class to make the state machine logic explicit and inspectable. This makes the demo more educational - interviewers can see exactly how the CLOSED/OPEN/HALF_OPEN transitions work without digging into library internals.

**Consequences:** More code to maintain, but full control over state transitions and easier to explain during a technical interview.

---

## ADR-002: Failure-rate routing (not round-robin)

**Status:** Accepted

**Context:** When routing a transaction across providers, a naive approach is round-robin. But this ignores provider health.

**Decision:** Sort providers by current failure rate (ascending) before each transaction. The healthiest provider is always tried first.

**Consequences:** Transactions naturally flow to the most reliable provider. Provider C (40% failure rate) gets fewer requests as its circuit breaker fills up.

---

## ADR-003: WebSocket via STOMP over SockJS

**Status:** Accepted

**Context:** Real-time transaction updates require push from server to browser. Options: polling, SSE, WebSocket.

**Decision:** Use Spring WebSocket with STOMP messaging protocol, SockJS as transport fallback. This allows HTTP long-polling fallback in environments that block WebSockets.

**Consequences:** Requires @stomp/stompjs + sockjs-client on the frontend. More complex than SSE but more flexible.

---

## ADR-004: Anomaly detection in a REQUIRES_NEW transaction

**Status:** Accepted

**Context:** Anomaly detection runs after the main transaction. If anomaly detection fails, the transaction should still be saved.

**Decision:** AnomalyDetectionService.checkAndFlag() runs in its own @Transactional(REQUIRES_NEW). Any exception is caught and logged without affecting the main transaction commit.

**Consequences:** Anomaly detection failures are non-fatal. The main transaction always commits first.

---

## ADR-005: H2 not used - PostgreSQL from day one

**Status:** Accepted

**Context:** Many demo projects use H2 in-memory for simplicity, then struggle to migrate.

**Decision:** Use PostgreSQL via Docker from the start. docker-compose.yml provides a ready-to-run DB with no setup friction.

**Consequences:** Requires Docker. But the schema is production-realistic from day one.
