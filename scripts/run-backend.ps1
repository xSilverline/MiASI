param(
  [int] $Port = 8080
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$backendVerify = Join-Path $repoRoot "backend\scripts\verify-backend.ps1"

$mavenArgs = @(
  "-Djava.version=21",
  "-Dspring-boot.run.workingDirectory=$repoRoot",
  "-Dspring-boot.run.arguments=--server.port=$Port",
  "spring-boot:run"
)

& $backendVerify -MavenArgs $mavenArgs
