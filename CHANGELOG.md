# Changelog

All notable changes to PayRoute are documented here.

This project follows [Semantic Versioning](https://semver.org/) for release tagging
and [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## [Unreleased]

### Added
- `useLocalStorage` hook — persist form values (amount, source) across page reloads
- `useThrottle` hook — prevent rapid re-submissions on payment form and burst simulator
- `utils/format.js` — shared currency (INR) and time formatting utilities with `timeAgo` helper
- `frontend/.env.example` — documents all Vite environment variables for new contributors
- `CHANGELOG.md` — this file

### Changed
- `AnomalyLog` — refactored to use shared `formatTime` / `formatINR` from `utils/format.js`
- `LiveTransactionFeed` — refactored to use shared `formatTime` / `formatINR` from `utils/format.js`
- `App.jsx` — payment form now persists amount and source via `useLocalStorage`

### Fixed
- Nothing yet

---

## [1.0.0] — 2026-08-26

### Added
- Initial implementation: Spring Boot backend, React frontend, PostgreSQL
- Circuit breaker (manual sliding-window implementation)
- Three simulated payment providers (A: low-risk, B: medium-risk, C: high-risk)
- Success-rate–ordered routing engine
- Idempotency key deduplication (DB UNIQUE constraint)
- Anomaly detection: large amounts, burst-from-source, repeated failures
- Real-time WebSocket feed (STOMP over SockJS)
- Docker Compose setup for one-command local deployment
- Vercel deployment config for frontend
- Burst simulator UI for tripping circuit breakers in demo
