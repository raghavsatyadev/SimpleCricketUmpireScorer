<#
.SYNOPSIS
  Interactive developer environment doctor and setup script on Windows.
.DESCRIPTION
  Automatically verifies prerequisites:
  - Git & Submodules
  - Git hooks (.githooks)
  - JDK 21 & JAVA_HOME
  - Android SDK, NDK 30.0.15729638, CMake 4.1.2, ANDROID_HOME & platform-tools (adb)
  - Connected Android devices/emulators
  - Python 3.10+ & uv
  - GitHub CLI login and repo access
  - ARTEMIS clone, API key and MCP registration
  - ARTEMIS model: Gemini (local Qwen fallback) or local Qwen only, sized to your GPU
  - ktfmt (fetched by scripts/ci-local.sh)
  - Links .agents/skills/ into .claude/skills/ so Claude Code finds the skills
  
  Offers interactive automated fixes and provides exact environment variable instructions.
#>

param(
  [switch]$NonInteractive,
  # Which model ARTEMIS uses: 'gemini' (local Qwen as quota fallback) or 'qwen' (local only).
  [ValidateSet('gemini', 'qwen')][string]$ArtemisModel,
  # Decision model for the agents: 'nimble', 'tev1' (4B or 0.8B by GPU), 'laya', 'jev' (hosted only)
  # or 'none'. Default: by hardware.
  [ValidateSet('nimble', 'tev1', 'laya', 'jev', 'none')][string]$DecisionModel
)

$ErrorActionPreference = "Continue"

function Write-Header ($text) {
  Write-Host ""
  Write-Host "==================================================================" -ForegroundColor Cyan
  Write-Host "  $text" -ForegroundColor White
  Write-Host "==================================================================" -ForegroundColor Cyan
}

function Write-Pass ($text) {
  Write-Host "  [OK] $text" -ForegroundColor Green
}

function Write-Warn ($text) {
  Write-Host "  [WARN] $text" -ForegroundColor Yellow
}

function Write-Fail ($text) {
  Write-Host "  [FAIL] $text" -ForegroundColor Red
}

function Write-Info ($text) {
  Write-Host "  [INFO] $text" -ForegroundColor DarkGray
}

function Prompt-Fix ($promptText) {
  if ($NonInteractive) { return $false }
  Write-Host ""
  $response = Read-Host "  --> $promptText (y/N)"
  return ($response -eq 'y' -or $response -eq 'Y')
}

$repoRoot = (Resolve-Path "$PSScriptRoot\..").Path
$kit = @{ APP_MODULE = ":app"; APP_VARIANT = "Debug"; NDK_VERSION = "" }
$kitFile = Join-Path $repoRoot "agent-kit.env"
if (Test-Path $kitFile) { Get-Content $kitFile | ForEach-Object { if ($_ -match '^\s*([A-Z_]+)=(.*)$') { $kit[$Matches[1]] = $Matches[2].Trim('"') } } }
Set-Location $repoRoot

Write-Header "Android Agent Kit - Interactive Developer Environment Setup"
Write-Info "Repository root: $repoRoot"
Write-Info "Platform: Windows $([System.Environment]::OSVersion.Version)"

$issuesFound = 0

# -------------------------------------------------------------
# 1. Git & Submodules
# -------------------------------------------------------------
Write-Header "1. Git Configuration & Submodules"
$gitCmd = Get-Command git -ErrorAction SilentlyContinue
if (-not $gitCmd) {
  Write-Fail "Git is not installed or not in PATH."
  Write-Info "Install Git for Windows from: https://git-scm.com/download/win"
  $issuesFound++
} else {
  Write-Pass "Git is installed: $($gitCmd.Source)"

  # Long paths: some submodules exceed MAX_PATH on Windows.
  if ((git config core.longpaths) -ne "true") {
    Write-Warn "git core.longpaths is not enabled (submodule checkouts can fail with 'Filename too long')."
    if (Prompt-Fix "Enable git core.longpaths globally now?") {
      git config --global core.longpaths true
      Write-Pass "Enabled git core.longpaths."
    } else {
      Write-Info "Manual command: git config --global core.longpaths true"
      $issuesFound++
    }
  } else {
    Write-Pass "git core.longpaths is enabled."
  }

  if ((Test-Path (Join-Path $repoRoot ".gitmodules")) -and ((git submodule status) -match '^-')) {
    Write-Warn "git submodules are not initialized."
    if (Prompt-Fix "Initialize git submodules now?") { git submodule update --init --recursive }
    else { Write-Info "Manual command: git submodule update --init --recursive"; $issuesFound++ }
  }

  # Check git hooks path
  $hooksPath = git config core.hooksPath
  if ($hooksPath -ne ".githooks") {
    Write-Warn "Git hooks path is not set to .githooks (currently: '$hooksPath')"
    if (Prompt-Fix "Set git core.hooksPath to .githooks now?") {
      git config core.hooksPath .githooks
      Write-Pass "Configured git core.hooksPath to .githooks."
    } else {
      Write-Info "Manual command: git config core.hooksPath .githooks"
    }
  } else {
    Write-Pass "Git hooks path is configured (.githooks)."
  }
}

# -------------------------------------------------------------
# 2. Java / JDK 21
# -------------------------------------------------------------
Write-Header "2. Java Development Kit (JDK 21)"
$javaHome = $env:JAVA_HOME
if ([string]::IsNullOrWhiteSpace($javaHome)) {
  $machineJavaHome = [System.Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
  $userJavaHome = [System.Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
  $javaHome = if ($machineJavaHome) { $machineJavaHome } else { $userJavaHome }
}

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
  $javaVerOutput = & java -version 2>&1 | Out-String
  Write-Pass "Java executable found: $($javaCmd.Source)"
  Write-Info ($javaVerOutput.Trim() -split "`n")[0]
  if ([string]::IsNullOrWhiteSpace($javaHome)) {
    $parentDir = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
    if (Test-Path "$parentDir\release") {
      $javaHome = $parentDir
    }
  }
} else {
  Write-Fail "Java executable not found in PATH."
  $issuesFound++
}

if (-not [string]::IsNullOrWhiteSpace($javaHome) -and (Test-Path $javaHome)) {
  Write-Pass "JAVA_HOME is set: $javaHome"
} else {
  Write-Warn "JAVA_HOME is not set or directory does not exist."
  Write-Info "To set JAVA_HOME permanently on Windows PowerShell, run:"
  Write-Host '    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Path\To\jdk-21", "User")' -ForegroundColor Yellow
  Write-Host '    [System.Environment]::SetEnvironmentVariable("Path", $env:Path + ";$env:JAVA_HOME\bin", "User")' -ForegroundColor Yellow
  $issuesFound++
}

# -------------------------------------------------------------
# 3. Android SDK, NDK, and CMake
# -------------------------------------------------------------
Write-Header "3. Android SDK, NDK 30.0.15729638 & CMake 4.1.2"
$androidHome = $env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($androidHome)) {
  $androidHome = $env:ANDROID_SDK_ROOT
}
if ([string]::IsNullOrWhiteSpace($androidHome) -and (Test-Path "$repoRoot\local.properties")) {
  $localPropLine = Get-Content "$repoRoot\local.properties" | Where-Object { $_ -match "^sdk\.dir=(.*)$" }
  if ($localPropLine) {
    $rawPath = ($localPropLine -replace "^sdk\.dir=", "").Trim()
    $cleanPath = $rawPath.Replace("\:", ":").Replace("\\", "\")
    if (Test-Path $cleanPath) {
      $androidHome = $cleanPath
    }
  }
}
if ([string]::IsNullOrWhiteSpace($androidHome)) {
  $defaultSdk = "$env:LOCALAPPDATA\Android\Sdk"
  if (Test-Path $defaultSdk) {
    $androidHome = $defaultSdk
  }
}

if (-not [string]::IsNullOrWhiteSpace($androidHome) -and (Test-Path $androidHome)) {
  Write-Pass "Android SDK located at: $androidHome"
  
  # Check platform-tools (adb)
  $adbExe = Join-Path $androidHome "platform-tools\adb.exe"
  $adbCmd = Get-Command adb -ErrorAction SilentlyContinue
  if ($adbCmd) {
    Write-Pass "adb is available in PATH: $($adbCmd.Source)"
  } elseif (Test-Path $adbExe) {
    Write-Warn "adb.exe exists at $adbExe, but platform-tools is not in PATH."
    Write-Info "Add to PATH with:"
    Write-Host "    [System.Environment]::SetEnvironmentVariable('Path', `$env:Path + ';$androidHome\platform-tools', 'User')" -ForegroundColor Yellow
  } else {
    Write-Fail "adb.exe not found in platform-tools. Install Android SDK Platform-Tools via Android Studio SDK Manager."
    $issuesFound++
  }

  # Check required NDK version (30.0.15729638)
  $requiredNdk = $kit.NDK_VERSION
  $ndkDir = Join-Path $androidHome "ndk\$requiredNdk"
  if (-not $requiredNdk) {
    Write-Info "No NDK_VERSION in agent-kit.env; skipping the NDK check."
  } elseif (Test-Path $ndkDir) {
    Write-Pass "Required Android NDK ($requiredNdk) found at $ndkDir"
  } else {
    Write-Warn "Android NDK $requiredNdk not found at $ndkDir."
    Write-Info "Install via Android Studio: Settings -> SDK Manager -> SDK Tools -> NDK (Side by side) -> Show Package Details -> 30.0.15729638"
    Write-Info "Or CLI: & `"$androidHome\cmdline-tools\latest\bin\sdkmanager.bat`" `"ndk;$requiredNdk`""
  }

  # Check CMake
  $cmakeDir = Join-Path $androidHome "cmake\4.1.2"
  if (Test-Path $cmakeDir) {
    Write-Pass "CMake 4.1.2 found at $cmakeDir"
  } else {
    Write-Info "CMake 4.1.2 directory not found at $cmakeDir. Gradle will attempt to resolve installed CMake versions."
  }
} else {
  Write-Fail "ANDROID_HOME / Android SDK was not found."
  Write-Info "Install Android Studio and set ANDROID_HOME:"
  Write-Host '    [System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")' -ForegroundColor Yellow
  $issuesFound++
}

# -------------------------------------------------------------
# 4. Connected Android Devices & Emulators
# -------------------------------------------------------------
Write-Header "4. Connected Android Devices / Emulators"
$adbCmd = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCmd) {
  $deviceLines = & adb devices -l | Where-Object { $_ -match "^\S+\s+(device|unauthorized|offline)" }
  if ($deviceLines) {
    Write-Pass "Detected active Android device(s):"
    foreach ($d in $deviceLines) {
      Write-Host "    $d" -ForegroundColor Green
    }
  } else {
    Write-Warn "No Android devices or emulators currently connected."
    Write-Info "Connect a physical phone via USB/Wi-Fi (with USB Debugging enabled) or start an Android Virtual Device (AVD)."
  }
}

# -------------------------------------------------------------
# 5. Python & uv
# -------------------------------------------------------------
Write-Header "5. Python 3.10+ and uv Package Manager"
$pythonCmd = Get-Command python -ErrorAction SilentlyContinue
if ($pythonCmd) {
  $pyVer = & python --version 2>&1
  Write-Pass "Python is installed: $pyVer ($($pythonCmd.Source))"
} else {
  Write-Warn "Python was not found in PATH."
  Write-Info "Install Python 3.11+ from https://www.python.org/downloads/ or via winget: winget install Python.Python.3.12"
}

$uvCmd = Get-Command uv -ErrorAction SilentlyContinue
if ($uvCmd) {
  Write-Pass "uv package manager is installed: $($uvCmd.Source)"
} else {
  Write-Warn "uv is not installed. uv is used for Artemis mobile testing automation."
  if (Prompt-Fix "Install uv now via official PowerShell installer?") {
    powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
  } else {
    Write-Info "To install uv manually: powershell -ExecutionPolicy ByPass -c `"irm https://astral.sh/uv/install.ps1 | iex`""
  }
}

# -------------------------------------------------------------
# 6. GitHub CLI (issue -> PR workflow)
# -------------------------------------------------------------
Write-Header "6. GitHub CLI"
$ghCmd = Get-Command gh -ErrorAction SilentlyContinue
if (-not $ghCmd) {
  Write-Fail "gh is not installed. The bug -> PR workflow uses it to read issues and open PRs."
  Write-Info "Install: winget install --id GitHub.cli   then: gh auth login"
  $issuesFound++
} else {
  Write-Pass "gh is installed: $($ghCmd.Source)"
  # 'gh auth status' also fails on a stale inactive account; test the active one instead.
  $ghUser = (& gh api user -q .login 2>$null | Out-String).Trim()
  if ($LASTEXITCODE -ne 0 -or -not $ghUser) {
    Write-Fail "gh is not logged in. Run: gh auth login"
    $issuesFound++
  } else {
    $perm = (& gh repo view --json viewerPermission -q .viewerPermission 2>$null | Out-String).Trim()
    if ($perm -in @("ADMIN", "MAINTAIN", "WRITE")) {
      Write-Pass "gh is logged in as $ghUser with $perm access to this repo."
    } else {
      Write-Warn "gh is logged in but has '$perm' access here - you cannot push fix branches."
      Write-Info "Ask a maintainer for write access to the repository."
    }
  }
}

# -------------------------------------------------------------
# 7. ARTEMIS (on-device testing via MCP)
# -------------------------------------------------------------
Write-Header "7. ARTEMIS Mobile Testing"
$artemisHome = if ($env:ARTEMIS_HOME) { $env:ARTEMIS_HOME } else { Join-Path (Split-Path $repoRoot -Parent) "artemis" }
# Not beside the repo: use the clone the ARTEMIS MCP server is already registered from.
if (-not $env:ARTEMIS_HOME -and -not (Test-Path (Join-Path $artemisHome "mcp_server")) -and (Get-Command claude -ErrorAction SilentlyContinue)) {
  $mcpPath = (& claude mcp get artemis 2>$null | Select-String -Pattern '^\s*PYTHONPATH=(.+)$' | Select-Object -First 1)
  if ($mcpPath) { $artemisHome = $mcpPath.Matches[0].Groups[1].Value.Trim() }
}
$artemisEnv = Join-Path $artemisHome ".env"
if (-not (Test-Path (Join-Path $artemisHome "mcp_server"))) {
  Write-Fail "ARTEMIS not found at $artemisHome (set ARTEMIS_HOME if it lives elsewhere)."
  if (Prompt-Fix "Clone google/artemis to $artemisHome now?") {
    git clone https://github.com/google/artemis.git $artemisHome
  }
  Write-Info "Then: cd $artemisHome; .\start.bat; uv run artemis mcp --install claude"
  $issuesFound++
} else {
  Write-Pass "ARTEMIS found at $artemisHome"

  if (Test-Path (Join-Path $artemisHome ".venv")) {
    Write-Pass "ARTEMIS virtualenv exists."
  } else {
    Write-Fail "ARTEMIS dependencies are not installed. Run: cd $artemisHome; .\start.bat"
    $issuesFound++
  }

  $hasKey = (Test-Path $artemisEnv) -and (Select-String -Path $artemisEnv -Pattern '^\s*(GEMINI_API_KEY|GOOGLE_API_KEY)\s*=\s*\S+' -Quiet)
  if ($hasKey) {
    Write-Pass "Gemini API key is set in $artemisEnv"
  } else {
    Write-Fail "No GEMINI_API_KEY in $artemisEnv"
    Write-Info "Get a key at https://aistudio.google.com/apikey and add: GEMINI_API_KEY=<key>"
    $issuesFound++
  }

  $claudeCmd = Get-Command claude -ErrorAction SilentlyContinue
  if ($claudeCmd) {
    & claude mcp get artemis *> $null
    if ($LASTEXITCODE -eq 0) {
      Write-Pass "ARTEMIS MCP server is registered with Claude Code."
    } else {
      Write-Fail "ARTEMIS MCP server is not registered with Claude Code."
      Write-Info "Run: cd $artemisHome; uv run artemis mcp --install claude   (then restart Claude Code)"
      $issuesFound++
    }
  } else {
    Write-Info "Claude Code CLI not found - register ARTEMIS with your agent: uv run artemis mcp --install <client>"
  }
  Write-Info "Final check from your agent: mobile_diagnose  (verdict must be 'ready' or 'degraded')"
}

# -------------------------------------------------------------
# 8. ARTEMIS model: Gemini (with local fallback) or local Qwen only
# -------------------------------------------------------------
Write-Header "8. ARTEMIS Model (Gemini / local Qwen)"
# Win32_VideoController.AdapterRAM caps at 4 GB, so only nvidia-smi gives real VRAM.
$vramGb = 0
$gpuName = "no NVIDIA GPU detected"
if (Get-Command nvidia-smi -ErrorAction SilentlyContinue) {
  $gpus = & nvidia-smi --query-gpu=name,memory.total --format=csv,noheader,nounits 2>$null
  foreach ($g in $gpus) {
    $parts = $g -split ",\s*"
    $gb = [math]::Round([double]$parts[1] / 1024, 1)
    if ($gb -gt $vramGb) { $vramGb = $gb; $gpuName = $parts[0] }
  }
}
$ramGb = [math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB)
Write-Info "Hardware: $gpuName ($vramGb GB VRAM), $ramGb GB RAM"

# Instruct (non-thinking) variants: an agent loop needs short, predictable replies.
# num_ctx is capped so model + context fit in VRAM; Ollama's default (the model's full
# 256K window) spills to CPU and is ~10x slower.
$baseModel = $null; $tier = $null; $numCtx = 32768
if ($vramGb -ge 24) { $baseModel = "qwen3-vl:30b-a3b-instruct"; $tier = "30b" }
elseif ($vramGb -ge 10) { $baseModel = "qwen3-vl:8b-instruct"; $tier = "8b" }
elseif ($vramGb -ge 6) { $baseModel = "qwen3-vl:4b-instruct"; $tier = "4b"; $numCtx = 16384 }
$localModel = if ($tier) { "qwen3-vl-artemis:$tier" } else { $null }

$modelScript = Join-Path $repoRoot "scripts\artemis_model.py"
$haveArtemis = Test-Path (Join-Path $artemisHome "mcp_server")
$haveLocal = $false

if (-not $localModel) {
  Write-Info "Under 6 GB VRAM: a local vision model is too slow for ARTEMIS; ARTEMIS stays on Gemini."
  Write-Info "For quota relief, add a second cloud key (e.g. OPEN_ROUTER_API_KEY) instead."
} else {
  Write-Pass "Local model for your GPU: $baseModel -> $localModel (context $numCtx)"
  $ollamaCmd = Get-Command ollama -ErrorAction SilentlyContinue
  $tags = $null
  if ($ollamaCmd) {
    try { $tags = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 3 -ErrorAction Stop } catch {}
  }
  if (-not $ollamaCmd) {
    Write-Info "Ollama not installed (optional). Get it from https://ollama.com/download/windows"
  } elseif (-not $tags) {
    Write-Warn "Ollama is installed but not running. Start it from the Start menu, then re-run."
  } elseif ($tags.models.name -contains $localModel) {
    Write-Pass "$localModel is ready in Ollama."
    $haveLocal = $true
  } elseif (Prompt-Fix "Download $baseModel (several GB) and create $localModel?") {
    & ollama pull $baseModel
    $modelfile = Join-Path $env:TEMP "artemis.Modelfile"
    "FROM $baseModel`nPARAMETER num_ctx $numCtx`nPARAMETER temperature 0" | Set-Content -Encoding ascii $modelfile
    & ollama create $localModel -f $modelfile
    $haveLocal = ($LASTEXITCODE -eq 0)
  } else {
    Write-Info "Later: ollama pull $baseModel, then re-run this script to create $localModel."
  }
}

if ($haveArtemis -and $haveLocal) {
  $mode = $ArtemisModel
  if (-not $mode -and -not $NonInteractive) {
    Write-Host ""
    Write-Host "  Which model should ARTEMIS drive the device with?" -ForegroundColor White
    Write-Host "    g) Gemini, with $localModel taking over when the quota runs out  (recommended)"
    Write-Host "    q) $localModel only - no Gemini calls, no quota, slower and less capable"
    Write-Host "    Enter) keep the current setting"
    $answer = Read-Host "  -->"
    if ($answer -eq 'g') { $mode = "gemini" } elseif ($answer -eq 'q') { $mode = "qwen" }
  }
  if ($mode) {
    & python $modelScript $mode --artemis-home $artemisHome --model $localModel
  } else {
    Write-Info ((& python $modelScript status --artemis-home $artemisHome) -join " ")
  }
  Write-Info "Switch any time: python scripts/artemis_model.py gemini|qwen|status"
}

# -------------------------------------------------------------
# 8b. Local decision model for the agent hooks (Nimble, Tev1 or Laya)
# -------------------------------------------------------------
# Optional. The hooks (scripts/nimble.sh) and the nimble skill ask a System One model quick yes/no
# and pick-one questions instead of reading long text. All run on Ollama 0.35+ except Laya.
# Measured on an RTX 5080 (60 tone samples, 10 log questions):
#   Nimble 9B   ~9 GB VRAM, ~8K-token window, tone 55/60, logs 10/10
#   Tev1 4B     ~4.7 GB,    ~2K-token window, tone 55/60, logs 10/10
#   Tev1 0.8B   ~0.9 GB,    ~2K-token window, tone 46/60, logs 10/10
#   Laya (pip)  ~1.3 GB,    ~512 tokens,      tone 46/60
# Jev (TypeSafe, hosted, paid per token) reads ~28K tokens; it is only used for text too long for
# the local model, never by the hooks. All of it is optional.
Write-Header "8b. Decision Model for Agents (Nimble / Tev1 / Laya local, Jev hosted)"
$tevModel = if ($vramGb -ge 6) { "tev1:4b" } else { "tev1:0.8b" }
$recommended = if ($vramGb -ge 12) { "nimble" } elseif ($vramGb -ge 2 -or $ramGb -ge 8) { "tev1" } else { "jev" }
$recText = @{ nimble = "Nimble + Jev"; tev1 = "$tevModel + Jev"; jev = "Jev only" }[$recommended]
Write-Info "Recommended for this PC ($vramGb GB VRAM, $ramGb GB RAM): $recText (Jev optional)."
$decision = $DecisionModel
if (-not $decision -and -not $NonInteractive) {
  Write-Host ""
  Write-Host "  Which decision model should the agents use?" -ForegroundColor White
  Write-Host "    n) Nimble - local, free, needs ~9 GB VRAM, reads ~6K tokens$(if ($recommended -eq 'nimble') { '  (recommended)' })"
  Write-Host "    t) $tevModel - local, free, $(if ($tevModel -eq 'tev1:4b') { '~5 GB VRAM' } else { '~1 GB' }), reads ~1.5K tokens$(if ($recommended -eq 'tev1') { '  (recommended)' })"
  Write-Host "    l) Laya   - local, free, pip server, reads ~400 tokens"
  Write-Host "    j) Jev only - hosted, paid per token, no local model$(if ($recommended -eq 'jev') { '  (recommended)' })"
  Write-Host "    s) skip - no decision model; hooks stay off"
  Write-Host "    Enter) $recText"
  $answer = Read-Host "  -->"
  $decision = switch ($answer) { 'n' { "nimble" } 't' { "tev1" } 'l' { "laya" } 'j' { "jev" } 's' { "none" } default { $recommended } }
}
if (-not $decision) { $decision = $recommended }
if ($decision -eq "none") { $decision = $null }

$ollamaModel = $null
# The hooks read these per-user variables; clear what the other choices set.
foreach ($v in "NIMBLE_URL", "NIMBLE_MODEL", "NIMBLE_MAX_BYTES", "NIMBLE_LOCAL") {
  if ($decision) { [System.Environment]::SetEnvironmentVariable($v, $null, "User") }
}

if (-not $decision) {
  Write-Info "No decision model. The hooks stay off; nothing breaks. Re-run to choose one."
} elseif ($decision -eq "jev") {
  [System.Environment]::SetEnvironmentVariable("NIMBLE_LOCAL", "0", "User")
  Write-Pass "Jev only: nimble-ask goes to Jev; the hooks stay off (they never pay for Jev)."
} elseif ($decision -eq "nimble" -or $decision -eq "tev1") {
  if ($decision -eq "nimble") {
    $ollamaModel = "nimble"
    if ($vramGb -lt 12) { Write-Warn "Nimble needs ~9 GB VRAM while loaded; this PC has $vramGb GB. Expect CPU spill and slow answers." }
    Write-Pass "Nimble ($vramGb GB VRAM; it needs ~9 GB while loaded)."
    if ($localModel -and $vramGb -lt 18) {
      Write-Info "Nimble and $localModel do not both fit in VRAM; Ollama swaps them, so the first hook after an ARTEMIS run is slower."
    }
  } else {
    # Tev1's window is ~2K tokens: send the last 3.6 KB (dense Gradle logs run ~2 bytes a token).
    $ollamaModel = $tevModel
    [System.Environment]::SetEnvironmentVariable("NIMBLE_MODEL", $tevModel, "User")
    [System.Environment]::SetEnvironmentVariable("NIMBLE_MAX_BYTES", "3600", "User")
    Write-Pass "$tevModel ($vramGb GB VRAM, $ramGb GB RAM). Hooks use it (NIMBLE_MODEL, NIMBLE_MAX_BYTES set; restart your agent)."
    if ($vramGb -lt 2) { Write-Info "No usable GPU: $tevModel runs on the CPU. Untested here; expect slower answers." }
    Write-Info "Longer text goes to Jev when a key is set; otherwise nimble-ask reads only the tail."
  }
  $ollamaCmd = Get-Command ollama -ErrorAction SilentlyContinue
  $ollamaVer = if ($ollamaCmd) { ((& ollama --version 2>$null) -join " ") -replace '.*?(\d+\.\d+\.\d+).*', '$1' } else { $null }
  $tags = $null
  if ($ollamaCmd) {
    try { $tags = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 3 -ErrorAction Stop } catch {}
  }
  if (-not $ollamaCmd) {
    Write-Info "Ollama not installed (optional). Get 0.35.0 or later from https://ollama.com/download/windows"
  } elseif ($ollamaVer -and [version]$ollamaVer -lt [version]"0.35.0") {
    Write-Warn "Ollama $ollamaVer is too old for the /v1/systemone endpoint. Update to 0.35.0 or later."
  } elseif (-not $tags) {
    Write-Warn "Ollama is installed but not running. Start it from the Start menu, then re-run."
  } elseif ($tags.models.name -contains $ollamaModel -or $tags.models.name -contains "${ollamaModel}:latest") {
    Write-Pass "$ollamaModel is ready in Ollama. The hooks use it at http://127.0.0.1:11434."
  } elseif (Prompt-Fix "Download $ollamaModel into Ollama ($(@{ 'nimble' = '~9.5 GB'; 'tev1:4b' = '~4.5 GB'; 'tev1:0.8b' = '~0.8 GB' }[$ollamaModel]))?") {
    & ollama pull $ollamaModel
  } else {
    Write-Info "Later: ollama pull $ollamaModel"
  }
} else {
  $layaEnv = Join-Path $env:USERPROFILE "laya-env"
  $device = if ($vramGb -ge 4) { "cuda" } else { "cpu" }
  Write-Pass "Laya fits this PC ($gpuName, $vramGb GB VRAM, $ramGb GB RAM; runs on $device)."
  if ($device -eq "cpu") { Write-Info "No CUDA GPU: Laya on the CPU is untested here and slower." }
  if (Test-Path (Join-Path $layaEnv "Scripts\laya-serve.exe")) {
    Write-Pass "Laya is installed in $layaEnv."
  } elseif (Prompt-Fix "Create $layaEnv and install Laya with PyTorch (~3 GB download)?") {
    & python -m venv $layaEnv
    $py = Join-Path $layaEnv "Scripts\python.exe"
    & $py -m pip install --upgrade pip
    if ($device -eq "cuda") {
      & $py -m pip install torch --index-url https://download.pytorch.org/whl/cu128
    } else {
      & $py -m pip install torch --index-url https://download.pytorch.org/whl/cpu
    }
    & $py -m pip install "laya[serve]"   # [serve] brings fastapi + uvicorn for laya-serve
  } else {
    Write-Info "Later: re-run this script and answer yes to install Laya."
  }
  $start = Join-Path $layaEnv "start-laya.ps1"
  if ((Test-Path (Join-Path $layaEnv "Scripts\laya-serve.exe")) -and -not (Test-Path $start)) {
    @"
# Starts the Laya System One server on 127.0.0.1:8000 (loopback only). -Background logs to laya-env\logs.
param([switch]`$Background)
`$envDir = Join-Path `$env:USERPROFILE 'laya-env'
`$env:LAYA_HOST = '127.0.0.1'; `$env:LAYA_PORT = '8000'; `$env:LAYA_MODELS = 'english'
`$env:LAYA_DEVICE = '$device'; `$env:LAYA_PRELOAD = '1'; `$env:HF_HUB_DISABLE_SYMLINKS_WARNING = '1'
`$exe = Join-Path `$envDir 'Scripts\laya-serve.exe'
if (`$Background) {
  `$logDir = Join-Path `$envDir 'logs'; New-Item -ItemType Directory -Force -Path `$logDir | Out-Null
  Start-Process -FilePath `$exe -WindowStyle Hidden -RedirectStandardOutput (Join-Path `$logDir 'out.log') -RedirectStandardError (Join-Path `$logDir 'err.log')
} else { & `$exe }
"@ | Set-Content -Encoding utf8 $start
    Write-Pass "Wrote $start"
  }
  # Point the hooks at Laya: same /v1/systemone API, but a 512-token window.
  [System.Environment]::SetEnvironmentVariable("NIMBLE_URL", "http://127.0.0.1:8000", "User")
  [System.Environment]::SetEnvironmentVariable("NIMBLE_MODEL", "laya", "User")
  [System.Environment]::SetEnvironmentVariable("NIMBLE_MAX_BYTES", "1800", "User")
  Write-Info "Hooks now use Laya (NIMBLE_URL, NIMBLE_MODEL, NIMBLE_MAX_BYTES set for your user; restart your agent)."
  Write-Info "Start it: powershell -ExecutionPolicy Bypass -File `"$start`" -Background"
}

# Jev (TypeSafe, hosted): optional, paid per input token. nimble-ask sends text there only when it
# is too long for the local model (or there is none); the hooks never do. The key stays in a
# per-user file, never in the repo.
if ($decision) {
  $jevKeyFile = Join-Path $env:USERPROFILE ".config\typesafe\api_key"
  if ($env:JEV_API_KEY -or (Test-Path $jevKeyFile)) {
    Write-Pass "Jev: a TypeSafe API key is set. Spend is logged in ~\.config\typesafe\usage.log."
  } elseif ($NonInteractive) {
    Write-Info "Jev (optional): no TypeSafe API key. Add one later in $jevKeyFile."
  } else {
    $secure = Read-Host "  --> Paste a TypeSafe API key for Jev (Enter to skip; add it later)" -AsSecureString
    $key = [System.Net.NetworkCredential]::new("", $secure).Password.Trim()
    if ($key) {
      New-Item -ItemType Directory -Force -Path (Split-Path $jevKeyFile) | Out-Null
      Set-Content -Path $jevKeyFile -Value $key -NoNewline -Encoding ascii
      & icacls $jevKeyFile /inheritance:r /grant:r "$($env:USERNAME):(R,W)" *> $null
      Write-Pass "Saved the key to $jevKeyFile (readable by you only)."
    } else {
      Write-Info "Jev skipped. Later: put the key in $jevKeyFile, or set JEV_API_KEY."
    }
  }
  if (Get-Command claude -ErrorAction SilentlyContinue) {
    if (-not (& claude plugin list 2>$null | Select-String -Quiet "typesafe")) {
      if (Prompt-Fix "Install the TypeSafe skill for Claude Code (how to build with Jev)?") {
        & claude plugin marketplace add typesafe-ai/skills
        & claude plugin install typesafe@typesafe-ai
      }
    } else { Write-Pass "TypeSafe skill is installed for Claude Code." }
  }
  Write-Info "Gemini/Antigravity: npx skills add typesafe-ai/skills --skill typesafe-ai (pick your agent)."
}

# jgl = jg (jevgrep) with the local model judging: finds code by meaning, nothing leaves the PC.
if ($decision -and $decision -ne "jev") {
  $haveJg = [bool](Get-Command jg -ErrorAction SilentlyContinue)
  $haveRg = [bool](Get-Command rg -ErrorAction SilentlyContinue)
  if ($haveJg -and $haveRg) {
    Write-Pass "jg and ripgrep are installed; ~/.nimble/jgl searches code by meaning with $decision."
  } elseif (Prompt-Fix "Install jg (npm) and ripgrep (winget) for search by meaning?") {
    if (-not $haveJg) { & npm install -g @remotehost/jg }
    if (-not $haveRg) { & winget install --id BurntSushi.ripgrep.MSVC -e --silent --accept-package-agreements --accept-source-agreements }
    Write-Info "Open a new terminal so PATH picks up rg."
  } else {
    Write-Info "Later: npm install -g @remotehost/jg; winget install BurntSushi.ripgrep.MSVC"
  }
}

# Global skills (global_skills/, e.g. `nimble`) work in every project, so they live in each
# user's global skill folders: Claude Code, Gemini/Antigravity, Codex. Installed only when asked.
$globalSkills = Get-ChildItem (Join-Path $repoRoot "global_skills") -Directory |
  Where-Object { Test-Path (Join-Path $_.FullName "SKILL.md") } | ForEach-Object { $_.Name }
$missingSkills = $globalSkills | Where-Object { $n = $_; @(".claude", ".gemini", ".agents") |
  Where-Object { -not (Test-Path (Join-Path $env:USERPROFILE "$_\skills\$n\SKILL.md")) } }
$bash = Get-Command bash -ErrorAction SilentlyContinue
if (-not $missingSkills) {
  Write-Pass "Global skills are installed ($($globalSkills -join ', ')). Re-run global_skills/install.sh after a pull that changes them."
} elseif (-not $bash) {
  Write-Warn "bash (Git Bash) not found; install Git for Windows, then run: bash global_skills/install.sh"
} elseif (Prompt-Fix "Install the global skills ($($missingSkills -join ', ')) for Claude Code, Gemini and Codex (every project)?") {
  & bash (Join-Path $repoRoot "global_skills/install.sh")
  Write-Pass "Restart your agent. Check: echo hi | bash ~/.nimble/nimble-ask yesno `"Is this a greeting?`""
} else {
  Write-Warn "Skipped. Missing global skills: $($missingSkills -join ', '). Later: bash global_skills/install.sh"
}
if ($decision) {
  if ($ollamaModel) { Write-Info "Free the GPU after use: bash ~/.nimble/nimble-off $ollamaModel (Claude Code does it on session end)." }
}

# -------------------------------------------------------------
# 9. Kotlin Formatting (ktfmt)
# -------------------------------------------------------------
Write-Header "9. Kotlin Formatting (ktfmt)"
Write-Pass "Nothing to install: 'bash scripts/ci-local.sh --fix' downloads the ktfmt version CI uses."
if (Get-Command ktfmt -ErrorAction SilentlyContinue) {
  Write-Warn "A ktfmt is on your PATH - don't use it for this repo; its version may disagree with CI."
}

# -------------------------------------------------------------
# 10. Agent Skills for Claude Code
# -------------------------------------------------------------
# Skills live in .agents/skills/ (Gemini CLI reads it). Claude Code only reads .claude/skills/,
# so link each skill there. Junctions need no admin rights or Developer Mode.
Write-Header "10. Agent Skills for Claude Code"
$claudeSkills = Join-Path $repoRoot ".claude\skills"
New-Item -ItemType Directory -Force -Path $claudeSkills | Out-Null
$linked = 0
foreach ($skill in Get-ChildItem -Directory (Join-Path $repoRoot ".agents\skills")) {
  $link = Join-Path $claudeSkills $skill.Name
  if (Test-Path $link) { continue }
  New-Item -ItemType Junction -Path $link -Target $skill.FullName | Out-Null
  $linked++
}
Write-Pass "Claude Code sees every skill in .agents/skills/ ($linked new link(s) in .claude/skills/)."

# -------------------------------------------------------------
# Summary & Environment Variables Guide
# -------------------------------------------------------------
Write-Header "Environment Variables Reference Guide"
Write-Host "  To permanently set environment variables on Windows (run in Administrator PowerShell):" -ForegroundColor Cyan
Write-Host "    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'User')"
Write-Host "    [System.Environment]::SetEnvironmentVariable('ANDROID_HOME', '$env:LOCALAPPDATA\Android\Sdk', 'User')"
Write-Host '    [System.Environment]::SetEnvironmentVariable(''Path'', $env:Path + ";$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools", ''User'')'
Write-Host ""
Write-Host "  Alternatively, use GUI: press Win+R -> sysdm.cpl -> Advanced -> Environment Variables" -ForegroundColor DarkGray
Write-Host ""

Write-Header "Setup Doctor Summary"
if ($issuesFound -eq 0) {
  Write-Host "  ALL ESSENTIAL PREREQUISITES ARE MET! You are ready to build." -ForegroundColor Green
  Write-Host "  Next steps:" -ForegroundColor White
  Write-Host "    1. Build app:    .\gradlew.bat :$($kit.APP_MODULE.TrimStart(':')):assemble$($kit.APP_VARIANT) --no-daemon --console=plain" -ForegroundColor Cyan
  Write-Host "    2. Run tests:    .\gradlew.bat :$($kit.APP_MODULE.TrimStart(':')):test$($kit.APP_VARIANT)UnitTest --no-daemon --console=plain" -ForegroundColor Cyan
  Write-Host "    3. Format code:  bash scripts/ci-local.sh --fix" -ForegroundColor Cyan
} else {
  Write-Host "  Found $issuesFound issue(s) that need attention. Please review the warnings above." -ForegroundColor Yellow
}
Write-Host ""
