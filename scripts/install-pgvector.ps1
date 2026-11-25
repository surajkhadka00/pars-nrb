# Installs pgvector v0.8.7 for local PostgreSQL 18 (Windows x64).
# Self-elevates to Administrator, verifies SHA-256, copies files, creates the extension.
# WARNING: third-party community binary built from official pgvector source. Dev use;
# production requires DBA approval and your own build/security review.

$ErrorActionPreference = 'Stop'

$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host 'Requesting Administrator privileges...' -ForegroundColor Yellow
    $script = "`"$($MyInvocation.MyCommand.Path)`""
    Start-Process -FilePath 'powershell.exe' -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File $script" -Verb RunAs
    exit
}

$expectedHash = '54e767b3f6f1b9a2b61363f999d1cc090a975831b9a052487d2dca65456ee925'
$url = 'https://github.com/1Henryc/pgvector-windows-binaries/releases/download/v0.8.7/pgvector-v0.8.7-pg18-windows-x64.zip'
$work = Join-Path $env:TEMP 'pars-pgvector-install'
$zip = Join-Path $work 'pgvector.zip'
$pgRoot = 'C:\Program Files\PostgreSQL\18'
$psql = Join-Path $pgRoot 'bin\psql.exe'

New-Item -ItemType Directory -Force -Path $work | Out-Null
Write-Host 'Downloading pgvector...' -ForegroundColor Cyan
Invoke-WebRequest -Uri $url -OutFile $zip

$actual = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
if ($actual -ne $expectedHash) {
    throw "CHECKSUM MISMATCH. Expected $expectedHash but got $actual. Installation aborted."
}
Write-Host 'SHA-256 verified.' -ForegroundColor Green

$extract = Join-Path $work 'extracted'
Expand-Archive -Path $zip -DestinationPath $extract -Force

Write-Host 'Stopping PostgreSQL service...' -ForegroundColor Cyan
Stop-Service -Name 'postgresql-x64-18' -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Copy-Item (Join-Path $extract 'lib\vector.dll') (Join-Path $pgRoot 'lib\vector.dll') -Force
Copy-Item (Join-Path $extract 'share\extension\*') (Join-Path $pgRoot 'share\extension\') -Force

Write-Host 'Starting PostgreSQL service...' -ForegroundColor Cyan
Start-Service -Name 'postgresql-x64-18'
Start-Sleep -Seconds 3

$env:PGPASSWORD = '123456'
& $psql -h localhost -U postgres -d nrb_pars -c 'CREATE EXTENSION IF NOT EXISTS vector;' -c "SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';"
if ($LASTEXITCODE -ne 0) { throw 'Failed to create vector extension.' }

Write-Host 'pgvector installed successfully.' -ForegroundColor Green
Write-Host 'Next: run scripts\setup-ollama.ps1' -ForegroundColor Yellow
