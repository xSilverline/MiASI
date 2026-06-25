$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$backendDir = Join-Path $repoRoot "backend"
$frontendDir = Join-Path $repoRoot "frontend"

Write-Host "== Backend verify =="
& powershell -ExecutionPolicy Bypass -File (Join-Path $backendDir "scripts/verify-backend.ps1")

Write-Host "== Frontend build =="
Push-Location $frontendDir
try {
  if (-not (Test-Path "node_modules")) {
    npm ci
  }

  npm run build
} finally {
  Pop-Location
}
