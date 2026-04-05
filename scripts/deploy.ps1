param(
    [string]$SshKeyPath = (Join-Path $PSScriptRoot '..\test-news.key'),
    [string]$SshUser = 'ubuntu',
    [string]$SshHost = '168.138.133.43',
    [string]$PublicBaseUrl = 'http://168.138.133.43',
    [string]$RemoteAppDir = '/home/ubuntu/noticia-popular',
    [string]$EnvFilePath = '',
    [string]$BackendImage = 'noticia-popular-backend:deploy',
    [string]$FrontendImage = 'noticia-popular-frontend:deploy',
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

if ([string]::IsNullOrWhiteSpace($EnvFilePath)) {
    $EnvFilePath = Join-Path $projectRoot '.env'
}

$EnvFilePath      = (Resolve-Path $EnvFilePath).Path
$SshKeyPath       = (Resolve-Path $SshKeyPath).Path
$ComposeFilePath  = Join-Path $projectRoot 'docker-compose.deploy.yml'
$backendDockerfile  = Join-Path $projectRoot 'backend\Dockerfile'
$frontendDockerfile = Join-Path $projectRoot 'frontend\Dockerfile'

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )
    Write-Host ">> $Command $($Arguments -join ' ')" -ForegroundColor Cyan
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao executar: $Command $($Arguments -join ' ')"
    }
}

function New-Utf8File {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][AllowEmptyString()][string[]]$Lines
    )
    $encoding = New-Object System.Text.UTF8Encoding($false)
    $content = [string]::Join("`n", $Lines)
    [System.IO.File]::WriteAllText($Path, $content, $encoding)
}

function New-SecureTemporarySshKey {
    param(
        [Parameter(Mandatory = $true)][string]$SourcePath,
        [Parameter(Mandatory = $true)][string]$TempDirectory
    )
    $targetPath = Join-Path $TempDirectory 'ssh-deploy.key'
    Copy-Item -LiteralPath $SourcePath -Destination $targetPath -Force

    $currentAccount = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $acl = New-Object System.Security.AccessControl.FileSecurity
    $acl.SetOwner([System.Security.Principal.NTAccount]$currentAccount)
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule(
        $currentAccount,
        [System.Security.AccessControl.FileSystemRights]::FullControl,
        [System.Security.AccessControl.AccessControlType]::Allow
    )
    $acl.SetAccessRuleProtection($true, $false)
    $acl.AddAccessRule($rule)
    Set-Acl -LiteralPath $targetPath -AclObject $acl
    return $targetPath
}

function Get-NormalizedUrl {
    param([Parameter(Mandatory = $true)][string]$Url)
    return $Url.Trim().TrimEnd('/')
}

function Get-EnvValue {
    param(
        [Parameter(Mandatory = $true)][string[]]$Lines,
        [Parameter(Mandatory = $true)][string]$Key
    )
    $line = $Lines | Where-Object { $_ -match "^$([regex]::Escape($Key))=" } | Select-Object -First 1
    if (-not $line) { return $null }
    return ($line -split '=', 2)[1].Trim()
}

function Test-RequiredEnvValue {
    param(
        [Parameter(Mandatory = $true)][string[]]$Lines,
        [Parameter(Mandatory = $true)][string]$Key
    )
    $val = Get-EnvValue -Lines $Lines -Key $Key
    if ([string]::IsNullOrWhiteSpace($val)) {
        throw "Variavel obrigatoria ausente ou vazia no .env: $Key"
    }
}

# --- Validacoes iniciais ---

if (-not (Test-Path $SshKeyPath)) {
    throw "Chave SSH nao encontrada: $SshKeyPath"
}

if (-not (Test-Path $EnvFilePath)) {
    throw ".env nao encontrado: $EnvFilePath"
}

$publicBaseUrl = Get-NormalizedUrl -Url $PublicBaseUrl
$publicUri     = [System.Uri]$publicBaseUrl
$publicHost    = $publicUri.Host

# URL do backend para o Angular (sem proxy nginx, acesso direto na porta 8080)
$backendApiUrl = "${publicBaseUrl}:8080/api/v1"
if ($publicUri.Port -eq 8080) {
    $backendApiUrl = "${publicBaseUrl}/api/v1"
}

$envLines = @(
    Get-Content $EnvFilePath | Where-Object {
        -not [string]::IsNullOrWhiteSpace($_) -and
        -not $_.TrimStart().StartsWith('#')
    }
)

Test-RequiredEnvValue -Lines $envLines -Key 'PG_PASSWORD'
Test-RequiredEnvValue -Lines $envLines -Key 'JWT_SECRET'

# Monta .env de deploy sobrescrevendo variaveis de URL
$deployLines = @($envLines | Where-Object {
    $_ -notmatch '^CORS_ORIGINS=' -and
    $_ -notmatch '^NG_APP_GOOGLE_CLIENT_ID='
})
$deployLines += "CORS_ORIGINS=$publicBaseUrl"

$googleClientId = Get-EnvValue -Lines $envLines -Key 'GOOGLE_CLIENT_ID'
if (-not [string]::IsNullOrWhiteSpace($googleClientId)) {
    $deployLines += "NG_APP_GOOGLE_CLIENT_ID=$googleClientId"
}

# --- Diretorio temporario ---

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("noticia-popular-deploy-" + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

$backendTar      = Join-Path $tempRoot 'backend-image.tar'
$frontendTar     = Join-Path $tempRoot 'frontend-image.tar'
$remoteEnvFile   = Join-Path $tempRoot '.env.deploy'
$remoteCompose   = Join-Path $tempRoot 'docker-compose.yml'
$remoteDeploySh  = Join-Path $tempRoot 'remote-deploy.sh'
$secureSshKey    = New-SecureTemporarySshKey -SourcePath $SshKeyPath -TempDirectory $tempRoot

$sshArgs = @(
    '-i', $secureSshKey,
    '-o', 'IdentitiesOnly=yes',
    '-o', 'StrictHostKeyChecking=accept-new'
)

try {
    # Verifica docker no servidor
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1"
    ))

    New-Utf8File -Path $remoteEnvFile -Lines $deployLines
    Copy-Item -LiteralPath $ComposeFilePath -Destination $remoteCompose -Force

    if (-not $SkipBuild) {
        Write-Host "`n==> Buildando backend..." -ForegroundColor Yellow
        Invoke-NativeCommand -Command 'docker' -Arguments @(
            'build', '--pull',
            '-t', $BackendImage,
            '-f', $backendDockerfile,
            (Join-Path $projectRoot 'backend')
        )

        Write-Host "`n==> Buildando frontend (API_URL=$backendApiUrl)..." -ForegroundColor Yellow
        Invoke-NativeCommand -Command 'docker' -Arguments @(
            'build', '--pull',
            '--build-arg', "NG_APP_API_URL=$backendApiUrl",
            '--build-arg', "NG_APP_GOOGLE_CLIENT_ID=$(Get-EnvValue -Lines $envLines -Key 'GOOGLE_CLIENT_ID')",
            '-t', $FrontendImage,
            '-f', $frontendDockerfile,
            (Join-Path $projectRoot 'frontend')
        )
    }

    Write-Host "`n==> Exportando imagens..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'docker' -Arguments @('image', 'save', '-o', $backendTar,  $BackendImage)
    Invoke-NativeCommand -Command 'docker' -Arguments @('image', 'save', '-o', $frontendTar, $FrontendImage)

    $remoteDeployScript = @"
set -euo pipefail

cd '$RemoteAppDir'

echo '==> Carregando imagens...'
docker load -i backend-image.tar
docker load -i frontend-image.tar
docker pull postgres:16-alpine

echo '==> Subindo servicos...'
docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml up -d --remove-orphans

echo '==> Limpando tars...'
rm -f backend-image.tar frontend-image.tar

echo '==> Status dos containers:'
docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml ps
"@
    New-Utf8File -Path $remoteDeploySh -Lines ($remoteDeployScript -split "`r?`n")

    Write-Host "`n==> Criando diretorio remoto..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "mkdir -p '$RemoteAppDir'"
    ))

    Write-Host "`n==> Enviando arquivos (SCP)..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'scp' -Arguments ($sshArgs + @(
        $remoteCompose,
        $remoteEnvFile,
        $backendTar,
        $frontendTar,
        $remoteDeploySh,
        "${SshUser}@${SshHost}:$RemoteAppDir/"
    ))

    Write-Host "`n==> Executando deploy remoto..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "mv '$RemoteAppDir/.env.deploy' '$RemoteAppDir/.env' && bash '$RemoteAppDir/remote-deploy.sh' && rm -f '$RemoteAppDir/remote-deploy.sh'"
    ))

    Write-Host "`nDeploy concluido!" -ForegroundColor Green
    Write-Host "  Frontend: $publicBaseUrl" -ForegroundColor Green
    Write-Host "  Backend:  $backendApiUrl" -ForegroundColor Green
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
