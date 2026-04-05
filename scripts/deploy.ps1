param(
    [string]$SshKeyPath = (Join-Path $PSScriptRoot '..\test-news.key'),
    [string]$SshUser = 'ubuntu',
    [string]$SshHost = '168.138.133.43',
    # Dominio com HTTPS (ex: 'meusite.com.br'). Deixe vazio para usar HTTP com o IP.
    [string]$Domain = '',
    [string]$GitHubPagesOrigin = 'https://wmakeouthill.github.io',
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

$EnvFilePath        = (Resolve-Path $EnvFilePath).Path
$SshKeyPath         = (Resolve-Path $SshKeyPath).Path
$ComposeFilePath    = Join-Path $projectRoot 'docker-compose.deploy.yml'
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

# --- URLs de acordo com o modo (HTTP ou HTTPS) ---

$hasHttps  = -not [string]::IsNullOrWhiteSpace($Domain)
$origin    = "http://$SshHost"
$certName  = $SshHost   # nome do cert no letsencrypt (IP ou dominio)

if ($hasHttps) {
    $origin   = "https://$Domain"
    $certName = $Domain
    Write-Host "Modo: HTTPS - dominio: $Domain" -ForegroundColor Cyan
} else {
    Write-Host "Modo: HTTP  - ip: $SshHost" -ForegroundColor Cyan
    Write-Host "Para HTTPS execute: .\scripts\enable-https.ps1" -ForegroundColor DarkYellow
}

$corsOrigins = @($origin)
if (-not [string]::IsNullOrWhiteSpace($GitHubPagesOrigin)) {
    $corsOrigins += $GitHubPagesOrigin
}
$corsOrigins = $corsOrigins | Select-Object -Unique

# --- Validacoes iniciais ---

if (-not (Test-Path $SshKeyPath)) {
    throw "Chave SSH nao encontrada: $SshKeyPath"
}

if (-not (Test-Path $EnvFilePath)) {
    throw ".env nao encontrado: $EnvFilePath"
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
    $_ -notmatch '^NG_APP_GOOGLE_CLIENT_ID=' -and
    $_ -notmatch '^NG_APP_API_URL='
})
$deployLines += "CORS_ORIGINS=$($corsOrigins -join ',')"

$googleClientId = Get-EnvValue -Lines $envLines -Key 'GOOGLE_CLIENT_ID'
if (-not [string]::IsNullOrWhiteSpace($googleClientId)) {
    $deployLines += "NG_APP_GOOGLE_CLIENT_ID=$googleClientId"
}

# --- Configs nginx ---

# HTTP: porta 80, sem SSL, proxia para os containers internos
$nginxHttpConf = @"
server {
    listen 80;
    server_name _;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /api/ {
        proxy_pass         http://backend:8080/api/;
        proxy_set_header   Host              `$host;
        proxy_set_header   X-Real-IP         `$remote_addr;
        proxy_set_header   X-Forwarded-For   `$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto `$scheme;
        proxy_read_timeout 60s;
    }

    location / {
        proxy_pass       http://frontend:80/;
        proxy_set_header Host `$host;
        proxy_http_version 1.1;
    }
}
"@

# HTTPS: redireciona 80->443, SSL com certificado Let's Encrypt
# Gerado sempre (para IP ou dominio) — remote script so usa se o cert existir
$nginxHttpsConf = @"
server {
    listen 80;
    server_name $certName;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://`$host`$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name $certName;

    ssl_certificate     /etc/letsencrypt/live/$certName/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$certName/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /api/ {
        proxy_pass         http://backend:8080/api/;
        proxy_set_header   Host              `$host;
        proxy_set_header   X-Real-IP         `$remote_addr;
        proxy_set_header   X-Forwarded-For   `$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto `$scheme;
        proxy_read_timeout 60s;
    }

    location / {
        proxy_pass       http://frontend:80/;
        proxy_set_header Host `$host;
        proxy_http_version 1.1;
    }
}
"@

# --- Diretorio temporario ---

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("noticia-popular-deploy-" + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

$backendTar         = Join-Path $tempRoot 'backend-image.tar'
$frontendTar        = Join-Path $tempRoot 'frontend-image.tar'
$remoteEnvFile      = Join-Path $tempRoot '.env.deploy'
$remoteCompose      = Join-Path $tempRoot 'docker-compose.yml'
$remoteDeploySh     = Join-Path $tempRoot 'remote-deploy.sh'
$nginxHttpConfFile  = Join-Path $tempRoot 'nginx-http.conf'
$nginxHttpsConfFile = Join-Path $tempRoot 'nginx-https.conf'
$secureSshKey       = New-SecureTemporarySshKey -SourcePath $SshKeyPath -TempDirectory $tempRoot

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

    New-Utf8File -Path $remoteEnvFile    -Lines $deployLines
    New-Utf8File -Path $nginxHttpConfFile -Lines ($nginxHttpConf -split "`r?`n")
    Copy-Item -LiteralPath $ComposeFilePath -Destination $remoteCompose -Force

    New-Utf8File -Path $nginxHttpsConfFile -Lines ($nginxHttpsConf -split "`r?`n")
    $scpFiles = @($remoteCompose, $remoteEnvFile, $nginxHttpConfFile, $nginxHttpsConfFile)

    if (-not $SkipBuild) {
        Write-Host "`n==> Buildando backend..." -ForegroundColor Yellow
        Invoke-NativeCommand -Command 'docker' -Arguments @(
            'build', '--pull',
            '-t', $BackendImage,
            '-f', $backendDockerfile,
            (Join-Path $projectRoot 'backend')
        )

        # URL relativa: nginx proxia /api/ para o backend em ambos HTTP e HTTPS
        Write-Host "`n==> Buildando frontend..." -ForegroundColor Yellow
        Invoke-NativeCommand -Command 'docker' -Arguments @(
            'build', '--pull',
            '--build-arg', 'NG_APP_API_URL=/api/v1',
            '--build-arg', "NG_APP_GOOGLE_CLIENT_ID=$googleClientId",
            '-t', $FrontendImage,
            '-f', $frontendDockerfile,
            (Join-Path $projectRoot 'frontend')
        )
    }

    Write-Host "`n==> Exportando imagens..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'docker' -Arguments @('image', 'save', '-o', $backendTar,  $BackendImage)
    Invoke-NativeCommand -Command 'docker' -Arguments @('image', 'save', '-o', $frontendTar, $FrontendImage)
    $scpFiles += @($backendTar, $frontendTar)

    # Script remoto: escolhe nginx.conf baseado em certificado existente em /etc/letsencrypt
    $remoteDeployScript = @"
set -euo pipefail

cd '$RemoteAppDir'
mkdir -p certbot-www

# Certbot instalado via snap armazena certs em /etc/letsencrypt (caminho do host,
# montado no container nginx). Checa se o cert para este host existe.
if sudo test -f "/etc/letsencrypt/live/$certName/fullchain.pem" 2>/dev/null; then
    echo '==> Certificado SSL encontrado, ativando HTTPS'
    cp nginx-https.conf nginx.conf
else
    echo '==> Sem certificado SSL, usando HTTP'
    cp nginx-http.conf nginx.conf
fi

echo '==> Carregando imagens...'
docker load -i backend-image.tar
docker load -i frontend-image.tar
docker pull postgres:16-alpine
docker pull nginx:1.27-alpine

echo '==> Subindo servicos...'
docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml up -d --remove-orphans

echo '==> Limpando tars...'
rm -f backend-image.tar frontend-image.tar

echo '==> Status dos containers:'
docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml ps
"@
    New-Utf8File -Path $remoteDeploySh -Lines ($remoteDeployScript -split "`r?`n")
    $scpFiles += $remoteDeploySh

    Write-Host "`n==> Criando diretorio remoto..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "mkdir -p '$RemoteAppDir'"
    ))

    Write-Host "`n==> Enviando arquivos (SCP)..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'scp' -Arguments ($sshArgs + $scpFiles + @("${SshUser}@${SshHost}:$RemoteAppDir/"))

    Write-Host "`n==> Executando deploy remoto..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "mv '$RemoteAppDir/.env.deploy' '$RemoteAppDir/.env' && bash '$RemoteAppDir/remote-deploy.sh' && rm -f '$RemoteAppDir/remote-deploy.sh'"
    ))

    Write-Host "`nDeploy concluido!" -ForegroundColor Green
    Write-Host "  Site: $origin" -ForegroundColor Green

    if (-not $hasHttps) {
        Write-Host ''
        Write-Host 'Para ativar HTTPS com o IP atual:' -ForegroundColor DarkYellow
        Write-Host "  .\scripts\enable-https.ps1" -ForegroundColor DarkYellow
    }
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
