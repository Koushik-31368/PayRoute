# Contributing to PayRoute

Thanks for your interest in contributing! This is a portfolio project, but pull requests for bug fixes, improvements, or new features are welcome.

---

## Getting Started

1. **Fork** the repo and clone your fork
2. Follow the **Quick Start** in [README.md](./README.md) to get the app running locally
3. Create a feature branch:
   ```bash
   git checkout -b feat/your-feature-name
   ```

---

## Commit Style

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructure (no behaviour change) |
| `style` | CSS / formatting changes |
| `docs` | Documentation only |
| `test` | Tests added or changed |
| `chore` | Build scripts, deps, config |

**Example:**
```
feat(ProviderHealthCard): add failure-rate trend arrow
```

---

## Frontend Dev Guidelines

- All API calls go in `src/api/payroute.js` — don't scatter `fetch()` calls in components
- All formatting helpers go in `src/utils/format.js` — don't duplicate `Intl.NumberFormat` calls
- Reusable hooks live in `src/hooks/` — prefix with `use`
- Component CSS lives next to the component file (e.g. `Foo.jsx` + `Foo.css`)
- Avoid inline styles except for dynamic values (colours from state)

---

## Backend Dev Guidelines

- New anomaly detection rules → add to `AnomalyType.java` enum + `AnomalyDetectionService.checkAndFlag()`
- New payment providers → add config to `application.properties` + update `ProviderSimulator.getConfig()`
- Circuit breaker settings are tunable per-provider via `application.properties`

---

## Code Review Checklist

- [ ] No secrets or env values committed
- [ ] Existing functionality still works (run `docker-compose up --build`)
- [ ] CSS changes are tested at mobile (640px) and tablet (1024px) widths
- [ ] Conventional commit message used
