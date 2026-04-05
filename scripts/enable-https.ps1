param(
    [string]$SshKeyPath = (Join-Path $PSScriptRoot '..\test-news.key'),
    [string]$SshUser = 'ubuntu',
    [string]$SshHost = '168.138.133.43',
    [string]$CertbotEmail = '',
    [string]$RemoteAppDir = '/home/ubuntu/noticia-popular'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$SshKeyPath      = (Resolve-Path $SshKeyPath).Path
$deployScriptPath = Join-Path $PSScriptRoot 'deploy.ps1'

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
    $targetPath = Join-Path $TempDirectory 'ssh-https.key'
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

if (-not (Test-Path $SshKeyPath)) {
    throw "Chave SSH nao encontrada: $SshKeyPath"
}

if (-not (Test-Path $deployScriptPath)) {
    throw "deploy.ps1 nao encontrado em $deployScriptPath"
}

# Argumentos certbot: com ou sem email
$certbotEmailArgs = if ([string]::IsNullOrWhiteSpace($CertbotEmail)) {
    '--register-unsafely-without-email'
} else {
    "--email $CertbotEmail"
}

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("noticia-popular-https-" + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null
$secureSshKey     = New-SecureTemporarySshKey -SourcePath $SshKeyPath -TempDirectory $tempRoot
$remoteSetupPath  = Join-Path $tempRoot 'enable-https-remote.sh'

$sshArgs = @(
    '-i', $secureSshKey,
    '-o', 'IdentitiesOnly=yes',
    '-o', 'StrictHostKeyChecking=accept-new'
)

# Script que recarrega o nginx apos renovacao automatica do certbot
$nginxReloadScript = @"
#!/usr/bin/env bash
set -euo pipefail
cd '$RemoteAppDir'
if docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml exec -T nginx nginx -s reload >/dev/null 2>&1; then
    echo 'nginx recarregado'
else
    docker compose --project-name noticia-popular --env-file .env -f docker-compose.yml restart nginx
    echo 'nginx reiniciado'
fi
"@

$remoteSetupLines = @(
    'set -euo pipefail',
    '',
    'echo "==> Atualizando pacotes..."',
    'sudo apt-get update -qq',
    '',
    'echo "==> Instalando snapd e certbot..."',
    'sudo apt-get install -y -qq snapd',
    'sudo snap list core >/dev/null 2>&1 || sudo snap install core',
    'sudo snap refresh core',
    'sudo snap list certbot >/dev/null 2>&1 || sudo snap install --classic certbot',
    'sudo snap refresh certbot',
    'sudo ln -sf /snap/bin/certbot /usr/local/bin/certbot',
    '',
    "mkdir -p '$RemoteAppDir/certbot-www'",
    '',
    '# Cria o script de reload do nginx (deploy-hook do certbot)',
    "cat > '$RemoteAppDir/reload-nginx.sh' <<'RELOADEOF'",
    $nginxReloadScript.TrimEnd(),
    'RELOADEOF',
    "chmod +x '$RemoteAppDir/reload-nginx.sh'",
    '',
    "CERT_PATH=/etc/letsencrypt/live/$SshHost/fullchain.pem",
    'if sudo test -f "$CERT_PATH"; then',
    "    echo '==> Certificado ja existe, pulando emissao'",
    'else',
    "    echo '==> Emitindo certificado para o IP $SshHost...'",
    "    echo '    (requer porta 80 acessivel e nginx no ar)'",
    '    sudo pkill -f certbot 2>/dev/null || true',
    "    sudo certbot certonly --non-interactive --agree-tos --preferred-profile shortlived --webroot -w '$RemoteAppDir/certbot-www' --ip-address $SshHost $certbotEmailArgs --deploy-hook '$RemoteAppDir/reload-nginx.sh'",
    '    if ! sudo test -f "$CERT_PATH"; then',
    "        echo 'ERRO: certificado nao encontrado. Diretorios em /etc/letsencrypt/live/:'",
    '        sudo ls /etc/letsencrypt/live/ 2>/dev/null || echo "(vazio)"',
    '        exit 1',
    '    fi',
    'fi',
    "echo '==> Cert em:' `$CERT_PATH",
    '',
    '# Ativa HTTPS no nginx imediatamente (nao depende de redeploy)',
    "echo '==> Ativando HTTPS no nginx...'",
    "cp '$RemoteAppDir/nginx-https.conf' '$RemoteAppDir/nginx.conf'",
    "docker compose --project-name noticia-popular --env-file '$RemoteAppDir/.env' -f '$RemoteAppDir/docker-compose.yml' exec -T nginx nginx -s reload 2>/dev/null || docker compose --project-name noticia-popular --env-file '$RemoteAppDir/.env' -f '$RemoteAppDir/docker-compose.yml' restart nginx",
    '',
    "echo '==> Atualizando CORS_ORIGINS para HTTPS...'",
    "sed -i 's|^CORS_ORIGINS=.*|CORS_ORIGINS=https://$SshHost|' '$RemoteAppDir/.env'",
    "docker compose --project-name noticia-popular --env-file '$RemoteAppDir/.env' -f '$RemoteAppDir/docker-compose.yml' up -d --force-recreate --no-deps backend",
    "echo '==> Backend reiniciado com CORS HTTPS'",
    '',
    "echo '==> HTTPS ativado com sucesso!'"
)

try {
    # Passo 1: garantir que o deploy HTTP esta no ar (nginx precisa estar servindo :80)
    Write-Host '==> Passo 1: deploy HTTP para nginx estar no ar na porta 80...' -ForegroundColor Yellow
    & $deployScriptPath `
        -SshKeyPath $SshKeyPath `
        -SshUser $SshUser `
        -SshHost $SshHost `
        -RemoteAppDir $RemoteAppDir `
        -SkipBuild

    # Passo 2: instalar certbot e emitir cert para o IP
    Write-Host "`n==> Passo 2: instalando certbot e emitindo certificado para $SshHost..." -ForegroundColor Yellow

    New-Utf8File -Path $remoteSetupPath -Lines $remoteSetupLines

    Invoke-NativeCommand -Command 'scp' -Arguments ($sshArgs + @(
        $remoteSetupPath,
        "${SshUser}@${SshHost}:$RemoteAppDir/enable-https-remote.sh"
    ))

    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        "bash '$RemoteAppDir/enable-https-remote.sh' && rm -f '$RemoteAppDir/enable-https-remote.sh'"
    ))

    Write-Host "`nHTTPS ativado!" -ForegroundColor Green
    Write-Host "  Acesse: https://$SshHost" -ForegroundColor Green
    Write-Host ''
    Write-Host 'Renovacao automatica configurada via certbot snap (systemd timer, 2x por dia).' -ForegroundColor DarkGray
    Write-Host "Deploy-hook: $RemoteAppDir/reload-nginx.sh recarrega o nginx apos cada renovacao." -ForegroundColor DarkGray

    if (-not [string]::IsNullOrWhiteSpace($CertbotEmail)) {
        Write-Host ''
        Write-Host 'Google OAuth — atualize as URIs no Google Cloud Console:' -ForegroundColor DarkYellow
        Write-Host "  Authorized JS origins:   https://$SshHost" -ForegroundColor DarkYellow
        Write-Host "  Authorized redirect URI: https://$SshHost/login/oauth2/code/google" -ForegroundColor DarkYellow
    }
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
