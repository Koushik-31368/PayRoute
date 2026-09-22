Set-Location "c:\Users\KOUSHIK REEDY\PayRoute"

function C($msg) {
    git add -A
    git commit -m $msg
    Write-Host "[OK] $msg" -ForegroundColor Green
}

# 1 - Add ANOMALY_POLL_INTERVAL constant
$f = "frontend\src\utils\constants.js"
Add-Content $f "`n/** Interval (ms) to refresh the anomaly list from REST API */`nexport const ANOMALY_POLL_INTERVAL_MS = 5000;" -Encoding UTF8
C "feat(constants): add ANOMALY_POLL_INTERVAL_MS constant"

# 2 - Add PROVIDER_GRID_COLS constant
Add-Content $f "`n/** Number of columns in the provider health grid */`nexport const PROVIDER_GRID_COLS = 3;" -Encoding UTF8
C "feat(constants): add PROVIDER_GRID_COLS constant"

# 3 - Add TOAST_DURATION constant
Add-Content $f "`n/** Default duration (ms) for transient toast/notification messages */`nexport const TOAST_DURATION_MS = 3000;" -Encoding UTF8
C "feat(constants): add TOAST_DURATION_MS constant"

# 4 - Add MAX_ERROR_MSG_LEN constant
Add-Content $f "`n/** Maximum characters shown in an error message tooltip */`nexport const MAX_ERROR_MSG_LEN = 120;" -Encoding UTF8
C "feat(constants): add MAX_ERROR_MSG_LEN constant"

# 5 - Add API_TIMEOUT_MS constant
Add-Content $f "`n/** Default timeout (ms) for REST API fetch calls */`nexport const API_TIMEOUT_MS = 10000;" -Encoding UTF8
C "feat(constants): add API_TIMEOUT_MS constant"

# 6 - Add APP_VERSION constant
Add-Content $f "`n/** Displayed application version string */`nexport const APP_VERSION = '1.0.0';" -Encoding UTF8
C "feat(constants): add APP_VERSION constant"

# 7 - Add CURRENCY_LOCALE constant
Add-Content $f "`n/** Locale string used for INR number formatting */`nexport const CURRENCY_LOCALE = 'en-IN';" -Encoding UTF8
C "feat(constants): add CURRENCY_LOCALE constant"

# 8 - Add @example to formatINR in format.js
$f = "frontend\src\utils\format.js"
(Get-Content $f -Raw) -replace "(@param \{boolean\} \[compact=false\])", "@example`n * // returns '₹5,000.00'`n * formatINR(5000)`n * `$1" | Set-Content $f -Encoding UTF8
C "docs(format): add @example to formatINR JSDoc"

# 9 - Add @example to timeAgo in format.js
(Get-Content $f -Raw) -replace "(@param \{string\} isoString\r?\n \*\/\r?\nexport function timeAgo)", "@param {string} isoString`n * @example`n * // returns '2s ago'`n * timeAgo(new Date(Date.now() - 2000).toISOString())`n */`nexport function timeAgo" | Set-Content $f -Encoding UTF8
C "docs(format): add @example to timeAgo JSDoc"

# 10 - Add @example to formatLatency in format.js
(Get-Content $f -Raw) -replace "(@param \{number\} ms\r?\n \* @returns)", "@param {number} ms`n * @example`n * formatLatency(450)  // '450ms'`n * formatLatency(1200) // '1.2s'`n * @returns" | Set-Content $f -Encoding UTF8
C "docs(format): add @example to formatLatency JSDoc"

# 11 - Add @version to App.jsx
$f = "frontend\src\App.jsx"
if (Test-Path $f) {
    $c = Get-Content $f -Raw
    if (-not ($c -match "@version")) {
        $c -replace "(?m)^import React", "// @version 1.0.0`nimport React" | Set-Content $f -Encoding UTF8
    }
}
C "docs(App): add @version comment to App.jsx"

# 12 - Add @version to main.jsx
$f = "frontend\src\main.jsx"
$c = Get-Content $f -Raw
if (-not ($c -match "@version")) {
    ("// @version 1.0.0`n" + $c) | Set-Content $f -Encoding UTF8
}
C "docs(main): add @version comment to main.jsx"

# 13 - Add @since to AnomalyController
$f = "backend\src\main\java\com\payroute\anomaly\AnomalyController.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class AnomalyController", "/** @since 1.0.0 */`npublic class AnomalyController" | Set-Content $f -Encoding UTF8
}
C "docs(AnomalyController): add @since 1.0.0 Javadoc"

# 14 - Add @since to AnomalyRepository
$f = "backend\src\main\java\com\payroute\anomaly\AnomalyRepository.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public interface AnomalyRepository", "/** @since 1.0.0 */`npublic interface AnomalyRepository" | Set-Content $f -Encoding UTF8
}
C "docs(AnomalyRepository): add @since 1.0.0 Javadoc"

# 15 - Add @since to TransactionController
$f = "backend\src\main\java\com\payroute\transaction\TransactionController.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class TransactionController", "/** @since 1.0.0 */`npublic class TransactionController" | Set-Content $f -Encoding UTF8
}
C "docs(TransactionController): add @since 1.0.0 Javadoc"

# 16 - Add @since to TransactionRepository
$f = "backend\src\main\java\com\payroute\transaction\TransactionRepository.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public interface TransactionRepository", "/** @since 1.0.0 */`npublic interface TransactionRepository" | Set-Content $f -Encoding UTF8
}
C "docs(TransactionRepository): add @since 1.0.0 Javadoc"

# 17 - Add @since to CircuitBreakerController
$f = "backend\src\main\java\com\payroute\circuitbreaker\CircuitBreakerController.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class CircuitBreakerController", "/** @since 1.0.0 */`npublic class CircuitBreakerController" | Set-Content $f -Encoding UTF8
}
C "docs(CircuitBreakerController): add @since 1.0.0 Javadoc"

# 18 - Add @since to CircuitBreakerRegistry
$f = "backend\src\main\java\com\payroute\circuitbreaker\CircuitBreakerRegistry.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class CircuitBreakerRegistry", "/** @since 1.0.0 */`npublic class CircuitBreakerRegistry" | Set-Content $f -Encoding UTF8
}
C "docs(CircuitBreakerRegistry): add @since 1.0.0 Javadoc"

# 19 - Add @since to AppConfig
$f = "backend\src\main\java\com\payroute\config\AppConfig.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class AppConfig", "/** @since 1.0.0 */`npublic class AppConfig" | Set-Content $f -Encoding UTF8
}
C "docs(AppConfig): add @since 1.0.0 Javadoc"

# 20 - Add @since to WebSocketConfig
$f = "backend\src\main\java\com\payroute\config\WebSocketConfig.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class WebSocketConfig", "/** @since 1.0.0 */`npublic class WebSocketConfig" | Set-Content $f -Encoding UTF8
}
C "docs(WebSocketConfig): add @since 1.0.0 Javadoc"

# 21 - Add @since to TransactionEventPublisher
$f = "backend\src\main\java\com\payroute\websocket\TransactionEventPublisher.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class TransactionEventPublisher", "/** @since 1.0.0 */`npublic class TransactionEventPublisher" | Set-Content $f -Encoding UTF8
}
C "docs(TransactionEventPublisher): add @since 1.0.0 Javadoc"

# 22 - Add @since to MockProviderController
$f = "backend\src\main\java\com\payroute\provider\MockProviderController.java"
$c = Get-Content $f -Raw
if (-not ($c -match "@since")) {
    $c -replace "public class MockProviderController", "/** @since 1.0.0 */`npublic class MockProviderController" | Set-Content $f -Encoding UTF8
}
C "docs(MockProviderController): add @since 1.0.0 Javadoc"

# 23 - Add footer note to SECURITY.md
$f = "SECURITY.md"
$c = Get-Content $f -Raw
if (-not ($c -match "PGP")) {
    ($c + "`n`n> For sensitive disclosures, contact the maintainer directly via GitHub private message.") | Set-Content $f -Encoding UTF8
}
C "docs(SECURITY): add private disclosure note"

# 24 - Add contributing tip to CONTRIBUTING.md
$f = "CONTRIBUTING.md"
$c = Get-Content $f -Raw
if (-not ($c -match "conventional commits")) {
    ($c + "`n`n## Commit Style`n`nThis project follows [Conventional Commits](https://www.conventionalcommits.org/). Use prefixes like `feat:`, `fix:`, `docs:`, `chore:` in your commit messages.") | Set-Content $f -Encoding UTF8
}
C "docs(CONTRIBUTING): add conventional commits style guide"

# 25 - Add ADR note
$f = "docs\ADR.md"
$c = Get-Content $f -Raw
if (-not ($c -match "Last reviewed")) {
    ($c + "`n`n---`n_Last reviewed: 2026-09-22_") | Set-Content $f -Encoding UTF8
}
C "docs(ADR): add last-reviewed timestamp"

# 26 - Add API versioning note to API.md
$f = "docs\API.md"
$c = Get-Content $f -Raw
if (-not ($c -match "API Version")) {
    ("## API Version`n`nCurrent version: **v1** (all endpoints under `/api/`)  `nNo breaking changes planned for v1.`n`n" + $c) | Set-Content $f -Encoding UTF8
}
C "docs(API): add API version section"

# Push all
Write-Host "`nPushing 26 commits..." -ForegroundColor Cyan
git push
Write-Host "Done! 26 contributions added today." -ForegroundColor Yellow
git log --oneline -30
