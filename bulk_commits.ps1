Set-Location "c:\Users\KOUSHIK REEDY\PayRoute"

function Commit ($msg) {
    git add -A
    git commit -m $msg
    Write-Host "  [OK] $msg" -ForegroundColor Green
}

# 1
$f = "frontend\src\utils\format.js"
(Get-Content $f -Raw) -replace "(?m)^/\*\*\r?\n \* Shared", "/**`n * @module format`n * Shared" | Set-Content $f -Encoding UTF8
Commit "docs(format): add @module tag to format.js"

# 2
$f = "frontend\src\utils\constants.js"
$add = "`n/** Known provider identifiers returned by the backend */`nexport const PROVIDER_NAMES = ['PROVIDER_A', 'PROVIDER_B', 'PROVIDER_C'];"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add PROVIDER_NAMES array"

# 3
$f = "frontend\src\utils\constants.js"
$add = "`n/** Default payment amount pre-filled in the submit form (INR) */`nexport const DEFAULT_AMOUNT = 5000;"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add DEFAULT_AMOUNT constant"

# 4
$f = "frontend\src\utils\constants.js"
$add = "`n/** Default source/customer-id pre-filled in the submit form */`nexport const DEFAULT_SOURCE = 'customer-001';"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add DEFAULT_SOURCE constant"

# 5
$f = "frontend\src\utils\constants.js"
$add = "`n/** Maximum transactions allowed in a single burst simulation */`nexport const MAX_BURST_COUNT = 100;"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add MAX_BURST_COUNT constant"

# 6
$f = "frontend\src\utils\constants.js"
$add = "`n/** Minimum transactions required for a burst simulation */`nexport const MIN_BURST_COUNT = 5;"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add MIN_BURST_COUNT constant"

# 7
$f = "frontend\src\hooks\useCountUp.js"
(Get-Content $f -Raw) -replace "@returns \{number\}", "@since 1.0.0`n * @returns {number}" | Set-Content $f -Encoding UTF8
Commit "docs(useCountUp): add @since 1.0.0 to JSDoc"

# 8
$f = "frontend\src\hooks\useDebounce.js"
(Get-Content $f -Raw) -replace "@returns \{any\}", "@since 1.0.0`n * @returns {any}" | Set-Content $f -Encoding UTF8
Commit "docs(useDebounce): add @since 1.0.0 to JSDoc"

# 9
$f = "frontend\src\hooks\useThrottle.js"
(Get-Content $f -Raw) -replace "@returns \{function\}", "@since 1.0.0`n * @returns {function}" | Set-Content $f -Encoding UTF8
Commit "docs(useThrottle): add @since 1.0.0 to JSDoc"

# 10
$f = "frontend\src\hooks\useWebSocket.js"
(Get-Content $f -Raw) -replace "export function useWebSocket", "// @since 1.0.0`nexport function useWebSocket" | Set-Content $f -Encoding UTF8
Commit "docs(useWebSocket): add @since comment"

# 11
$f = "frontend\src\hooks\useWindowSize.js"
(Get-Content $f -Raw) -replace "export function useWindowSize", "// @since 1.0.0`nexport function useWindowSize" | Set-Content $f -Encoding UTF8
Commit "docs(useWindowSize): add @since comment"

# 12
$f = "frontend\src\hooks\useOnlineStatus.js"
(Get-Content $f -Raw) -replace "export function useOnlineStatus", "// @since 1.0.0`nexport function useOnlineStatus" | Set-Content $f -Encoding UTF8
Commit "docs(useOnlineStatus): add @since comment"

# 13
$f = "frontend\src\hooks\useLocalStorage.js"
(Get-Content $f -Raw) -replace "@param \{any\}    defaultValue", "@since 1.0.0`n * @param {any}    defaultValue" | Set-Content $f -Encoding UTF8
Commit "docs(useLocalStorage): add @since 1.0.0 to JSDoc"

# 14
$f = "frontend\src\hooks\usePrevious.js"
(Get-Content $f -Raw) -replace "@param \{any\}  initialValue", "@since 1.0.0`n * @param {any}  initialValue" | Set-Content $f -Encoding UTF8
Commit "docs(usePrevious): add @since 1.0.0 to JSDoc"

# 15
$f = "frontend\vite.config.js"
Add-Content $f "`n// end of vite.config.js" -Encoding UTF8
Commit "chore(vite): add end-of-file marker comment"

# 16
$f = "backend\src\main\java\com\payroute\anomaly\AnomalyType.java"
(Get-Content $f -Raw) -replace "public enum AnomalyType", "/** @since 1.0.0 */`npublic enum AnomalyType" | Set-Content $f -Encoding UTF8
Commit "docs(AnomalyType): add @since 1.0.0 Javadoc"

# 17
$f = "backend\src\main\java\com\payroute\transaction\TransactionStatus.java"
(Get-Content $f -Raw) -replace "public enum TransactionStatus", "/** @since 1.0.0 */`npublic enum TransactionStatus" | Set-Content $f -Encoding UTF8
Commit "docs(TransactionStatus): add @since 1.0.0 Javadoc"

# 18
$f = "backend\src\main\java\com\payroute\PayRouteApplication.java"
if (-not ((Get-Content $f -Raw) -match "@author")) {
    (Get-Content $f -Raw) -replace "public class PayRouteApplication", "/** @author PayRoute Contributors */`npublic class PayRouteApplication" | Set-Content $f -Encoding UTF8
}
Commit "docs(PayRouteApplication): add @author Javadoc tag"

# 19
$f = "Makefile"
$content = Get-Content $f -Raw
if (-not ($content -match "# Usage:")) {
    "# Usage: make <target>`n" + $content | Set-Content $f -Encoding UTF8
}
Commit "docs(Makefile): add usage comment at top"

# 20
$f = "README.md"
$content = Get-Content $f -Raw
if (-not ($content -match "Last Updated")) {
    ($content -replace "(?m)^# PayRoute", "# PayRoute`n<!-- Last Updated: 2026-09-18 -->") | Set-Content $f -Encoding UTF8
}
Commit "docs(README): add last-updated metadata comment"

# 21
$f = "CHANGELOG.md"
$content = Get-Content $f -Raw
if (-not ($content -match "Breaking Changes")) {
    ($content -replace "(?m)^### Fixed", "### Breaking Changes`n- None`n`n### Fixed") | Set-Content $f -Encoding UTF8
}
Commit "docs(CHANGELOG): add explicit Breaking Changes section"

# 22
$f = "docs\RUNBOOK.md"
$content = Get-Content $f -Raw
if (-not ($content -match "GET /actuator")) {
    ($content + "`n`n## Health Check`n`nThe backend exposes a health endpoint at ``GET /actuator/health`` (if Spring Actuator is enabled).") | Set-Content $f -Encoding UTF8
}
Commit "docs(RUNBOOK): add health check endpoint note"

# 23
$f = "backend\src\main\java\com\payroute\provider\ProviderSimulator.java"
(Get-Content $f -Raw) -replace "@return          \{@link ProviderResult\}", "@return          {@link ProviderResult} containing the outcome and measured latency`n     * @throws InterruptedException if the thread is interrupted during simulated latency sleep" | Set-Content $f -Encoding UTF8
Commit "docs(ProviderSimulator): add @throws to simulate() Javadoc"

# 24
$f = "frontend\.env.example"
$content = Get-Content $f -Raw
if (-not ($content -match "VITE_ prefix")) {
    ("# Note: all client-side variables MUST be prefixed with VITE_ to be exposed by Vite.`n" + $content) | Set-Content $f -Encoding UTF8
}
Commit "docs(env.example): clarify VITE_ prefix requirement"

# 25
$f = "frontend\src\utils\constants.js"
$add = "`n/** Human-readable label shown in the UI for the OPEN circuit breaker state */`nexport const CB_OPEN_LABEL = 'Circuit Open';"
Add-Content $f $add -Encoding UTF8
Commit "feat(constants): add CB_OPEN_LABEL display string"

# Push
Write-Host "`nPushing all commits to GitHub..." -ForegroundColor Cyan
git push
Write-Host "Done! You now have 25+ contributions today." -ForegroundColor Yellow
git log --oneline -30
