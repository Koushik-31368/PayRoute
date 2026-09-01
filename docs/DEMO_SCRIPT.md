# PayRoute Demo Script

Estimated time: 4-5 minutes

---

## 0:00 - Intro (30s)

"PayRoute is a payment routing engine with a custom circuit breaker.
It routes transactions across three payment providers, automatically
bypassing degraded ones. I built this to demonstrate resilience patterns
you'd see in production payment infrastructure."

---

## 0:30 - Normal Operation (60s)

1. Show the dashboard - 3 provider health cards (all CLOSED)
2. Submit a transaction: amount=5000, source=demo-user
3. Watch it appear in Live Feed (SUCCESS via PROVIDER_A)
4. Point out: "Provider A has 10% failure rate so gets priority"

---

## 1:30 - Trip the Circuit Breaker (90s)

1. Click "Simulate Burst" - count=30, amount=1000
2. Narrate: "30 rapid transactions, PROVIDER_C fails ~40% of the time..."
3. Watch the PROVIDER_C bar chart fill up with red
4. Circuit breaker flips OPEN - "Now it's isolated"
5. Submit another transaction - watch it skip PROVIDER_C in attempts

---

## 3:00 - Anomaly Detection (60s)

1. Submit amount=200000 - "This exceeds the 100,000 threshold"
2. LARGE_AMOUNT anomaly appears in the Anomaly Log
3. Point out the Burst Anomaly from the burst test
4. "Anomaly detection runs in its own transaction - non-fatal"

---

## 4:00 - Recovery (30s)

1. Wait 30 seconds or explain: "After cooldown, circuit goes HALF_OPEN"
2. Next transaction probes PROVIDER_C
3. If it succeeds: circuit CLOSED again

---

## 4:30 - Technical Summary (30s)

Key decisions:
- Custom circuit breaker for transparency
- Failure-rate-ordered routing (not round-robin)
- STOMP/SockJS for real-time WebSocket updates
- Idempotency keys prevent duplicate charges on retry
