param(
  [string[]] $MavenArgs = @("clean", "verify")
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $projectRoot

function Get-JavaMajorVersion {
  param([string] $JavaExe)

  $versionOutput = & cmd /c "`"$JavaExe`" -version 2>&1" | Out-String
  if ($versionOutput -match 'version "([0-9]+)') {
    return [int] $Matches[1]
  }
  if ($versionOutput -match 'openjdk ([0-9]+)') {
    return [int] $Matches[1]
  }
  return 0
}

function Find-Jdk21 {
  $candidates = @()

  if ($env:JAVA_HOME) {
    $candidates += $env:JAVA_HOME
  }

  $adoptiumRoot = "C:\Program Files\Eclipse Adoptium"
  if (Test-Path $adoptiumRoot) {
    $candidates += Get-ChildItem -Path $adoptiumRoot -Directory -Filter "jdk-21*" |
        ForEach-Object { $_.FullName }
  }

  $javaFromPath = Get-Command java -ErrorAction SilentlyContinue
  if ($javaFromPath) {
    $javaHomeFromPath = Split-Path (Split-Path $javaFromPath.Source -Parent) -Parent
    $candidates += $javaHomeFromPath
  }

  foreach ($candidate in ($candidates | Where-Object { $_ } | Select-Object -Unique)) {
    $javaExe = Join-Path $candidate "bin\java.exe"
    if ((Test-Path $javaExe) -and (Get-JavaMajorVersion $javaExe) -ge 21) {
      return $candidate
    }
  }

  throw "JDK 21+ was not found. Install JDK 21 or set JAVA_HOME to a JDK 21+ directory."
}

function Get-MavenCommand {
  $globalMaven = Get-Command mvn -ErrorAction SilentlyContinue
  if ($globalMaven) {
    return $globalMaven.Source
  }

  $mavenVersion = "3.9.10"
  $mavenDir = Join-Path $projectRoot ".mvn\apache-maven-$mavenVersion"
  $mavenCmd = Join-Path $mavenDir "bin\mvn.cmd"

  if (-not (Test-Path $mavenCmd)) {
    $mvnCacheDir = Join-Path $projectRoot ".mvn"
    New-Item -ItemType Directory -Force -Path $mvnCacheDir | Out-Null

    $zipPath = Join-Path $mvnCacheDir "apache-maven-$mavenVersion-bin.zip"
    $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

    if (-not (Test-Path $zipPath)) {
      Write-Host "Downloading Apache Maven $mavenVersion..."
      Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    }

    Write-Host "Extracting Apache Maven $mavenVersion..."
    Expand-Archive -Path $zipPath -DestinationPath $mvnCacheDir -Force
  }

  return $mavenCmd
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

$jdkHome = Find-Jdk21
$env:JAVA_HOME = $jdkHome
$env:PATH = "$jdkHome\bin;$env:PATH"

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
& cmd /c "`"$env:JAVA_HOME\bin\java.exe`" -version 2>&1"

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
