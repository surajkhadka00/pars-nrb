# Installs a FREE local AI model provider (Ollama) and two models:
#   llama3.2:3b       - chat model (no API key, runs on your CPU)
#   nomic-embed-text  - embedding model (768 dimensions)
# No data leaves your computer.

$ErrorActionPreference = 'Stop'
Write-Host 'Installing Ollama (free local AI)...' -ForegroundColor Cyan

winget install --id Ollama.Ollama -e --accept-source-agreements --accept-package-agreements
if ($LASTEXITCODE -ne 0) { Write-Warning 'winget install returned non-zero; check if Ollama is already installed.' }

# Refresh PATH for this session
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

$ollama = Get-Command ollama -ErrorAction SilentlyContinue
if (-not $ollama) { throw 'Ollama not found on PATH. Open a new PowerShell and run this script again.' }

Write-Host 'Starting Ollama server in background...' -ForegroundColor Cyan
Start-Process -FilePath 'ollama.exe' -ArgumentList 'serve' -WindowStyle Hidden
Start-Sleep -Seconds 5

Write-Host 'Downloading chat model llama3.2:3b (about 2 GB)...' -ForegroundColor Cyan
ollama pull llama3.2:3b
if ($LASTEXITCODE -ne 0) { throw 'Failed to pull chat model.' }

Write-Host 'Downloading embedding model nomic-embed-text (~270 MB)...' -ForegroundColor Cyan
ollama pull nomic-embed-text
if ($LASTEXITCODE -ne 0) { throw 'Failed to pull embedding model.' }

Write-Host 'Installed models:' -ForegroundColor Green
ollama list
Write-Host 'Done. Now start the backend with: $env:PARS_AI_ENABLED="true"; $env:PARS_AI_PROVIDER="ollama"; $env:PARS_AI_EMBEDDING_DIMENSIONS="768"; mvn spring-boot:run' -ForegroundColor Yellow
