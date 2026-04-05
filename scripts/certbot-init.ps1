param(
    [Parameter(Mandatory = $true)]
    [string]$Domain,
    [Parameter(Mandatory = $true)]
    [string]$Email,
    [string]$SshKeyPath = (Join-Path $PSScriptRoot '..\test-news.key'),
    [string]$SshUser = 'ubuntu',
    [string]$SshHost = '168.138.133.43',
    [string]$RemoteAppDir = '/home/ubuntu/noticia-popular'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$SshKeyPath = (Resolve-Path $SshKeyPath).Path

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

function New-SecureTemporarySshKey {
    param(
        [Parameter(Mandatory = $true)][string]$SourcePath,
        [Parameter(Mandatory = $true)][string]$TempDirectory
    )
    $targetPath = Join-Path $TempDirectory 'ssh-certbot.key'
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

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("noticia-popular-certbot-" + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null
$secureSshKey = New-SecureTemporarySshKey -SourcePath $SshKeyPath -TempDirectory $tempRoot

$sshArgs = @(
    '-i', $secureSshKey,
    '-o', 'IdentitiesOnly=yes',
    '-o', 'StrictHostKeyChecking=accept-new'
)

# Script que roda no servidor:
# 1. Emite o certificado via webroot challenge (nginx ja precisa estar no ar na porta 80)
# 2. Substitui nginx.conf para HTTPS
# 3. Recarrega nginx
$certbotScript = @"
set -euo pipefail

DOMAIN="$Domain"
EMAIL="$Email"
APP_DIR="$RemoteAppDir"

echo "==> Verificando se o dominio `$DOMAIN resolve para este servidor..."
RESOLVED=`$(getent hosts "`$DOMAIN" | awk '{ print `$1 }' | head -1)
MY_IP=`$(curl -s ifconfig.me || curl -s icanhazip.com)
if [ "`$RESOLVED" != "`$MY_IP" ]; then
    echo "AVISO: `$DOMAIN resolve para `$RESOLVED, mas este servidor e `$MY_IP"
    echo "Certifique-se que o DNS esta apontado corretamente antes de continuar."
    read -r -p "Continuar mesmo assim? [s/N] " RESP
    [ "`$RESP" = "s" ] || [ "`$RESP" = "S" ] || exit 1
fi

echo "==> Emitindo certificado para `$DOMAIN..."
cd "`$APP_DIR"

docker run --rm \
    -v "`$APP_DIR/certbot-www:/var/www/certbot" \
    -v "`$APP_DIR/certbot-certs:/etc/letsencrypt" \
    certbot/certbot certonly \
        --webroot \
        --webroot-path /var/www/certbot \
        --domain "`$DOMAIN" \
        --email "`$EMAIL" \
        --agree-tos \
        --no-eff-email \
        --non-interactive

echo "==> Certificado emitido com sucesso!"
echo "==> Ativando config HTTPS no nginx..."

cp "`$APP_DIR/nginx-https.conf" "`$APP_DIR/nginx.conf"

docker compose --project-name noticia-popular -f "`$APP_DIR/docker-compose.yml" exec nginx nginx -s reload

echo ""
echo "==> HTTPS ativado! Acesse: https://`$DOMAIN"
echo ""
echo "Renovacao automatica: o container certbot renova a cada 12h."
echo "Para verificar: docker compose --project-name noticia-popular logs certbot"
"@

try {
    Write-Host "`nEmitindo certificado para $Domain..." -ForegroundColor Yellow
    Write-Host "ATENCAO: o nginx precisa estar no ar (porta 80 acessivel) para o desafio funcionar.`n" -ForegroundColor DarkYellow

    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        '-t',   # TTY para o read interativo funcionar
        "${SshUser}@${SshHost}",
        $certbotScript
    ))

    Write-Host "`nCertificado emitido! Proximos passos:" -ForegroundColor Green
    Write-Host "  Configure o Google OAuth com as URIs HTTPS:" -ForegroundColor Green
    Write-Host "    Authorized JS origins:  https://$Domain" -ForegroundColor Green
    Write-Host "    Authorized redirect URI: https://$Domain/login/oauth2/code/google" -ForegroundColor Green
    Write-Host ''
    Write-Host "  Agora refaca o deploy para bake a URL HTTPS no frontend:" -ForegroundColor Green
    Write-Host "    .\scripts\deploy.ps1 -Domain '$Domain'" -ForegroundColor Green
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
