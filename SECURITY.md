# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in PayRoute, please report it responsibly:

1. **Do NOT open a public GitHub issue** for security vulnerabilities
2. Contact the maintainer via GitHub profile
3. Include: description, steps to reproduce, and potential impact

## Security Architecture

PayRoute is a payment routing demo with several built-in safety mechanisms:

- **Idempotency Keys** - Prevent duplicate transaction processing on retry
- **Circuit Breaker** - Isolates failing payment providers automatically
- **Anomaly Detection** - Flags large amounts, burst activity, and repeated failures
- **Input Validation** - All request bodies validated at the controller layer

## Data Handling Notes

- This is a **demo application** - do not use real payment credentials
- Database credentials are stored in pplication.properties / environment variables
- The .env file is gitignored and should never be committed

## Dependencies

To audit backend dependencies:
```bash
cd backend && mvn dependency:tree
```

To audit frontend dependencies:
```bash
cd frontend && npm audit
```
