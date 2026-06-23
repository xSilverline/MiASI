param(
  [string[]] $MavenArgs = @("clean", "verify")
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $projectRoot

function Get-JavaVersionOutput {
  param([string] $JavaExe)

  $previousErrorActionPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  try {
    return (& $JavaExe -version 2>&1 | ForEach-Object { $_.ToString() }) -join [Environment]::NewLine
  } finally {
    $ErrorActionPreference = $previousErrorActionPreference
  }
}

function Get-JavaMajorVersion {
  param([string] $JavaExe)

  $versionOutput = Get-JavaVersionOutput $JavaExe
  if ($versionOutput -match 'version "1\.([0-9]+)') {
    return [int] $Matches[1]
  }
  if ($versionOutput -match 'version "([0-9]+)') {
    return [int] $Matches[1]
  }
  if ($versionOutput -match 'openjdk ([0-9]+)') {
    return [int] $Matches[1]
  }
  return 0
}

function Get-JavaExecutableFromHome {
  param([string] $JavaHome)

  if (-not $JavaHome) {
    return $null
  }

  $javaBinDir = Join-Path $JavaHome "bin"
  foreach ($javaName in @("java.exe", "java")) {
    $javaExe = Join-Path $javaBinDir $javaName
    if (Test-Path $javaExe) {
      return $javaExe
    }
  }

  return $null
}

function Has-Javac {
  param([string] $JavaHome)

  if (-not $JavaHome) {
    return $false
  }

  $javaBinDir = Join-Path $JavaHome "bin"
  foreach ($javacName in @("javac.exe", "javac")) {
    if (Test-Path (Join-Path $javaBinDir $javacName)) {
      return $true
    }
  }

  return $false
}

function Get-JavaHomeFromExecutable {
  param([string] $JavaExe)

  $javaBinDir = Split-Path $JavaExe -Parent
  $javaHome = Split-Path $javaBinDir -Parent
  if (Has-Javac $javaHome) {
    return $javaHome
  }

  return $null
}

function Get-JavaHomeCandidates {
  $candidates = @()

  Get-ChildItem Env: |
      Where-Object { $_.Name -eq "JAVA_HOME" -or $_.Name -like "JAVA_HOME_*" -or $_.Name -eq "JDK_HOME" } |
      ForEach-Object { $candidates += $_.Value }

  $javaCommand = Get-Command java -ErrorAction SilentlyContinue
  if ($javaCommand) {
    $javaHomeFromPath = Get-JavaHomeFromExecutable $javaCommand.Source
    if ($javaHomeFromPath) {
      $candidates += $javaHomeFromPath
    }
  }

  $programFilesRoots = @($env:ProgramFiles, ${env:ProgramFiles(x86)}, $env:ProgramW6432) |
      Where-Object { $_ -and (Test-Path $_) } |
      Select-Object -Unique

  foreach ($programFilesRoot in $programFilesRoots) {
    Get-ChildItem -Path $programFilesRoot -Directory -ErrorAction SilentlyContinue | ForEach-Object {
      if ($_.Name -like "jdk*") {
        $candidates += $_.FullName
      }

      Get-ChildItem -Path $_.FullName -Directory -Filter "jdk*" -ErrorAction SilentlyContinue |
          ForEach-Object { $candidates += $_.FullName }
    }
  }

  return $candidates | Where-Object { $_ } | Select-Object -Unique
}

function Find-Jdk21 {
  foreach ($candidate in Get-JavaHomeCandidates) {
    $javaExe = Get-JavaExecutableFromHome $candidate
    if ($javaExe -and (Has-Javac $candidate) -and (Get-JavaMajorVersion $javaExe) -ge 21) {
      return [PSCustomObject]@{
        Home = $candidate
        Java = $javaExe
      }
    }
  }

  throw "JDK 21+ was not found. Set JAVA_HOME/JDK_HOME to a JDK 21+ directory or add JDK 21+ to PATH."
}

function Get-MavenCommand {
  foreach ($wrapperName in @("mvnw.cmd", "mvnw")) {
    $wrapperPath = Join-Path $projectRoot $wrapperName
    if (Test-Path $wrapperPath) {
      return $wrapperPath
    }
  }

  $globalMaven = Get-Command mvn -ErrorAction SilentlyContinue
  if ($globalMaven) {
    return $globalMaven.Source
  }

  throw "Maven was not found. Install Maven and add mvn to PATH or add Maven Wrapper files to the backend directory."
}

function Restore-TestDatabaseFiles {
  $sourceDir = Join-Path $projectRoot "src\test\resources\database\databaseHardCopy"
  $targetDir = Join-Path $projectRoot "src\test\resources\database"

  if (-not (Test-Path $sourceDir)) {
    return
  }

  Get-ChildItem -Path $sourceDir -Filter "*.json" | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $targetDir $_.Name) -Force
  }
}

$jdk = Find-Jdk21
$env:JAVA_HOME = $jdk.Home
$javaExe = $jdk.Java
$javaHomeBin = Join-Path $env:JAVA_HOME "bin"
$env:PATH = "$javaHomeBin$([System.IO.Path]::PathSeparator)$env:PATH"

Write-Host "Using Java: $javaExe"
Write-Host (Get-JavaVersionOutput $javaExe)

$mavenCommand = Get-MavenCommand
Write-Host "Using Maven command: $mavenCommand"
$exitCode = 0
try {
  & $mavenCommand @MavenArgs
  $exitCode = $LASTEXITCODE
} finally {
  Restore-TestDatabaseFiles
}

exit $exitCode
