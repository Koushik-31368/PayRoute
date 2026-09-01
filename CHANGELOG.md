# Changelog

All notable changes to PayRoute are documented here.

This project follows [Semantic Versioning](https://semver.org/) for release tagging
and [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## [Unreleased]

### Added
- `useLocalStorage` hook â€” persist form values (amount, source) across page reloads
- `useThrottle` hook â€” prevent rapid re-submissions on payment form and burst simulator
- `utils/format.js` â€” shared currency (INR) and time formatting utilities with `timeAgo` helper
- `frontend/.env.example` â€” documents all Vite environment variables for new contributors
- `CHANGELOG.md` â€” this file

### Changed
- `AnomalyLog` â€” refactored to use shared `formatTime` / `formatINR` from `utils/format.js`
- `LiveTransactionFeed` â€” refactored to use shared `formatTime` / `formatINR` from `utils/format.js`
- `App.jsx` â€” payment form now persists amount and source via `useLocalStorage`

### Fixed
- Nothing yet

---

## [1.0.0] â€” 2026-08-26

### Added
- Initial implementation: Spring Boot backend, React frontend, PostgreSQL
- Circuit breaker (manual sliding-window implementation)
- Three simulated payment providers (A: low-risk, B: medium-risk, C: high-risk)
- Success-rateâ€“ordered routing engine
- Idempotency key deduplication (DB UNIQUE constraint)
- Anomaly detection: large amounts, burst-from-source, repeated failures
- Real-time WebSocket feed (STOMP over SockJS)
- Docker Compose setup for one-command local deployment
- Vercel deployment config for frontend
- Burst simulator UI for tripping circuit breakers in demo

## [Unreleased]

### Added
- SECURITY.md with vulnerability disclosure policy
- Makefile with common dev workflow commands
- docs/API.md with full endpoint reference
- docs/ADR.md with 5 architecture decision records
- docs/RUNBOOK.md with local dev quick-start guide
- docs/DEMO_SCRIPT.md with timestamped demo narration
- frontend/src/hooks/useDebounce.js
- frontend/src/hooks/useOnlineStatus.js
- frontend/src/hooks/usePrevious.js
- frontend/src/hooks/useWindowSize.js
- frontend/src/utils/constants.js with app-wide enums
- frontend/src/utils/format.js formatLatency() helper
- backend/.env.example for environment configuration
- .editorconfig for consistent code formatting
