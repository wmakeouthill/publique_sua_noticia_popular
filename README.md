# 📰 Portal de Notícias Populares

O **Portal de Notícias Populares** é uma aplicação completa (Full-Stack) voltada para a criação e leitura de notícias de forma rápida e moderna. O sistema possui arquitetura limpa e integra-se com inteligência artificial (Gemini) para acelerar a produção de conteúdo.

## 🛠️ Tecnologias Principais

- **Backend:** Java 21, Spring Boot 3.4, Clean Architecture
- **Frontend:** Angular 21, Standalone Components, Signals, UI Glassmorphism (Dark Mode)
- **Banco de Dados:** PostgreSQL 16
- **IA e Autenticação:** Gemini API, Google OAuth2
- **Infraestrutura:** Docker e Docker Compose

## GitHub Pages do Frontend

O frontend pode ser publicado no GitHub Pages consumindo o backend hospedado na Oracle VPS.

Pré-requisitos práticos:

- Backend exposto em HTTPS válido.
- `CORS_ORIGINS` no backend contendo `https://wmakeouthill.github.io`.
- Variável de repositório `NG_APP_API_URL` apontando para a API pública, por exemplo `https://api.seudominio.com/api/v1`.
- Variável de repositório `NG_APP_GOOGLE_CLIENT_ID` com o client id do Google, se o login Google estiver habilitado.

O workflow está em [.github/workflows/deploy-frontend-pages.yml](d:/publique_sua_noticia_popular/.github/workflows/deploy-frontend-pages.yml) e publica automaticamente a cada push na branch `main`.

---

## 🚀 Como Executar Localmente (com PowerShell)

Todo o ecossistema (Frontend, Backend e Banco de Dados) está configurado para subir via Docker Compose de forma simplificada.

### 1. Pré-requisitos

- Docker Desktop instalado e rodando.
- PowerShell aberto na pasta raiz do projeto (`d:\publique_sua_noticia_popular`).

### 2. Configuração do Ambiente (.env)

Antes de subir, garanta que o arquivo `.env` existe na raiz do projeto.
Se você acabou de clonar/criar, copie o arquivo de exemplo executando no PowerShell:

```powershell
Copy-Item .env.example -Destination .env
```

*(Opcional: edite o `.env` gerado com suas chaves reais da API do Gemini e Google OAuth caso queira testar estas integrações específicas).*

### 3. Subir os Contêineres

Com o Docker Desktop rodando, abra o PowerShell na raiz do projeto (`d:\publique_sua_noticia_popular`) e execute:

```powershell
# Inicia todos os serviços em segundo plano (Backend, Frontend e PostgreSQL)
docker-compose up -d --build
```

### 4. Acompanhar os Logs (Opcional mas recomendado)

Para garantir que o Spring Boot e o Angular iniciaram corretamente, você pode visualizar os logs:

```powershell
# Para ver os logs de todos os serviços
docker-compose logs -f

# Para ver logs de um serviço específico (exemplo: backend)
docker-compose logs -f backend
```

### 5. Acessar a Aplicação

Depois que os serviços estiverem prontos, acesse no seu navegador:

- **Frontend (UI):** [http://localhost:4300](http://localhost:4300)
- **Backend (API):** [http://localhost:8082/api/v1](http://localhost:8082/api/v1)
- **Health Check do Backend:** [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)

### 6. Parar a Aplicação

Quando terminar, você pode desligar tudo rodando o seguinte comando no PowerShell:

```powershell
# Desliga os contêineres e preserva os volumes do banco
docker-compose down

# Caso queira desligar e DELETAR os dados do banco (Zerar o DB)
docker-compose down -v
```

---

## 🐳 Docker Otimizado (Dev e Prod)

O repositório agora está separado por propósito:

- **Desenvolvimento local** (hot reload):
  - `backend/Dockerfile.dev`
  - `frontend/Dockerfile.dev`
  - `docker-compose.yml`
- **Produção** (imagens menores e startup mais previsível):
  - `backend/Dockerfile` (multi-stage)
  - `frontend/Dockerfile` (multi-stage + nginx)
  - `docker-compose.prod.yml`
  - `.dockerignore` em `backend/` e `frontend/`

### Subir em modo produção (teste local)

```powershell
docker compose -f docker-compose.prod.yml up -d --build
```

### Parar produção

```powershell
docker compose -f docker-compose.prod.yml down
```

### Dicas para instância com 1GB RAM

- O `docker-compose.prod.yml` já inclui `mem_limit` e `memswap_limit` por serviço.
- Garanta swap no host (exemplo 1GB a 2GB) para evitar OOM kill em picos.
- Em produção real, prefira não expor PostgreSQL publicamente (somente rede privada).
- Se necessário, desative serviços auxiliares (ex.: pgAdmin) no ambiente de produção.

---

## 📁 Estrutura do Repositório

```text
publique_sua_noticia_popular/
├── backend/          # API Java 21 com Spring Boot (Porta 8080)
├── frontend/         # Aplicação Angular 21 (Porta 4300)
├── docker-compose.yml# Orquestrador dos contêineres
├── .env              # Variáveis de ambiente
└── README.md         # Documentação atual
```
