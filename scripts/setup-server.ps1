param(
    [string]$SshKeyPath = (Join-Path $PSScriptRoot '..\test-news.key'),
    [string]$SshUser = 'ubuntu',
    [string]$SshHost = '168.138.133.43',
    [int]$SwapSizeGb = 2
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
    $targetPath = Join-Path $TempDirectory 'ssh-setup.key'
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

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("noticia-popular-setup-" + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null
$secureSshKey = New-SecureTemporarySshKey -SourcePath $SshKeyPath -TempDirectory $tempRoot

$sshArgs = @(
    '-i', $secureSshKey,
    '-o', 'IdentitiesOnly=yes',
    '-o', 'StrictHostKeyChecking=accept-new'
)

$setupScript = @"
set -euo pipefail

echo '==> Atualizando pacotes...'
sudo apt-get update -qq

# --- Docker ---
if command -v docker >/dev/null 2>&1; then
    echo '==> Docker ja instalado:'
    docker --version
else
    echo '==> Instalando Docker...'
    sudo apt-get install -y -qq ca-certificates curl gnupg lsb-release

    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    echo "deb [arch=`$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu `$(lsb_release -cs) stable" \
        | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    sudo systemctl enable docker
    sudo systemctl start docker

    echo '==> Docker instalado:'
    docker --version
fi

# Adiciona usuario ao grupo docker (sem precisar de sudo)
if ! groups $SshUser | grep -q docker; then
    echo '==> Adicionando $SshUser ao grupo docker...'
    sudo usermod -aG docker $SshUser
    echo '    (efetivo na proxima sessao SSH)'
fi

# --- Swap ---
SWAP_FILE=/swapfile
SWAP_SIZE="${SwapSizeGb}G"

if sudo swapon --show | grep -q `$SWAP_FILE; then
    echo "==> Swap ja configurado em `$SWAP_FILE:"
    sudo swapon --show
else
    echo "==> Configurando swap de `$SWAP_SIZE em `$SWAP_FILE..."

    if [ -f "`$SWAP_FILE" ]; then
        sudo swapoff `$SWAP_FILE 2>/dev/null || true
        sudo rm -f `$SWAP_FILE
    fi

    sudo fallocate -l `$SWAP_SIZE `$SWAP_FILE
    sudo chmod 600 `$SWAP_FILE
    sudo mkswap `$SWAP_FILE
    sudo swapon `$SWAP_FILE

    if ! grep -q `$SWAP_FILE /etc/fstab; then
        echo "`$SWAP_FILE none swap sw 0 0" | sudo tee -a /etc/fstab > /dev/null
    fi

    # Reduz agressividade do swap (mais lento para usar disco)
    sudo sysctl -w vm.swappiness=10 > /dev/null
    if ! grep -q 'vm.swappiness' /etc/sysctl.conf; then
        echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf > /dev/null
    fi

    echo '==> Swap configurado:'
    sudo swapon --show
    free -h
fi

# --- Firewall (iptables) ---
echo '==> Abrindo portas no firewall do SO...'
sudo iptables -C INPUT -p tcp --dport 80 -j ACCEPT 2>/dev/null || sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -C INPUT -p tcp --dport 443 -j ACCEPT 2>/dev/null || sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -C INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null || sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT

# Persiste regras no reboot
if command -v netfilter-persistent >/dev/null 2>&1; then
    sudo netfilter-persistent save
else
    sudo apt-get install -y -qq iptables-persistent
    sudo netfilter-persistent save
fi

echo ''
echo '==> Setup concluido!'
echo '    Docker: ok'
echo "    Swap:   `${SWAP_SIZE}"
echo '    Portas: 80, 443, 8080 abertas'
echo ''
echo 'IMPORTANTE: faca logout e login novamente (ou nova sessao SSH)'
echo 'para que o usuario $SshUser possa usar docker sem sudo.'
"@

try {
    Write-Host "`n==> Conectando em ${SshUser}@${SshHost}..." -ForegroundColor Yellow
    Invoke-NativeCommand -Command 'ssh' -Arguments ($sshArgs + @(
        "${SshUser}@${SshHost}",
        $setupScript
    ))
}
finally {
    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
