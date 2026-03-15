# 📰 Portal de Notícias Populares

Portal de notícias escalável com IA, Google OAuth2, editor slash command e feed personalizado.

## 🛠️ Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21 LTS + Spring Boot 3.4 + Clean Architecture + DDD |
| Frontend | Angular 20+ + Standalone + Signals |
| Banco | PostgreSQL 16 |
| Auth | Google OAuth2 + JWT |
| IA | Gemini API (texto + imagem) |
| Infra | Docker Compose |

## 📁 Estrutura

```
publique_sua_noticia_popular/
├── backend/          # Spring Boot (Clean Architecture)
├── frontend/         # Angular 20+ (Standalone)
├── docker-compose.yml
├── .env.example
└── regras-desenvolvimento-java-angular/
```

## 🚀 Como rodar localmente

### 1. Pré-requisitos

- Docker + Docker Compose
- Node 22 LTS (para desenvolvimento frontend sem Docker)
- JDK 21+ (para desenvolvimento backend sem Docker)

### 2. Configurar variáveis de ambiente

```bash
cp .env.example .env
# Edite o .env com suas chaves reais
```

**Variáveis obrigatórias:**
- `PG_PASSWORD` — senha do PostgreSQL
- `JWT_SECRET` — chave Base64 de no mínimo 32 chars
- `GOOGLE_CLIENT_ID` — Client ID do Google OAuth
- `GOOGLE_CLIENT_SECRET` — Client Secret do Google
- `GEMINI_API_KEY` — chave da Gemini API

### 3. Subir com Docker Compose

```bash
# Subir todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f backend

# Com pgAdmin (ferramenta visual BD)
docker-compose --profile tools up -d
```

### 4. Acessar

| Serviço | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api/v1 |
| Health Check | http://localhost:8080/actuator/health |
| pgAdmin | http://localhost:8081 (profile tools) |

## 🔧 Configuração Google OAuth2

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um projeto → `APIs & Services` → `Credentials`
3. Crie `OAuth 2.0 Client ID` (Web Application)
4. Origens JavaScript autorizadas: `http://localhost:4200`
5. URIs de redirecionamento: `http://localhost:4200/auth/callback`
6. Copie Client ID e Secret para o `.env`

## 🤖 Configuração Gemini API

1. Acesse [Google AI Studio](https://aistudio.google.com/)
2. Crie uma API Key
3. Adicione no `.env` como `GEMINI_API_KEY`

## 🐳 Otimização para Oracle Cloud (1GB RAM)

No servidor de produção, configure swap antes de iniciar:

```bash
# Criar swap de 2GB
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Configurar swappiness para minimizar uso de swap
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

**Limites de memória no Docker:**
- Backend: 512MB
- PostgreSQL: 384MB
- Sistema: ~128MB para SO e overhead

## 📐 Arquitetura Backend (Clean Architecture + DDD)

```
src/
├── kernel/                     # Domínio compartilhado
│   ├── domain/exceptions/      # Hierarquia de exceções
│   └── infrastructure/
│       ├── config/             # JacksonConfig, CorsConfig
│       ├── security/           # JWT, SecurityConfig
│       └── web/                # GlobalExceptionHandler
├── autenticacao/               # Módulo Auth
├── categorias/                 # Módulo Categorias
├── noticias/                   # Módulo Notícias
└── inteligenciaartificial/     # Módulo IA/Gemini
```

Cada módulo segue:
```
modulo/
├── domain/         # Entidades ricas, Value Objects, Exceções
├── application/    # Use Cases, DTOs, Ports
└── infrastructure/ # JPA, Controllers, Adapters externos
```

## 🔐 Endpoints da API

| Método | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/v1/auth/google` | Público |
| GET | `/api/v1/auth/perfil` | JWT |
| GET | `/api/v1/categorias` | Público |
| POST | `/api/v1/categorias` | ADMIN |
| GET | `/api/v1/noticias` | Público |
| GET | `/api/v1/noticias/{id}` | Público |
| POST | `/api/v1/noticias` | JWT |
| GET | `/api/v1/noticias/minhas` | JWT |
| POST | `/api/v1/ia/gerar-imagem` | JWT |
| POST | `/api/v1/ia/gerar-texto` | JWT |
| POST | `/api/v1/ia/refinar-texto` | JWT |

## 📋 Regras de Desenvolvimento

Ver pasta [`regras-desenvolvimento-java-angular/`](./regras-desenvolvimento-java-angular/)

- `regras.md` — Princípios fundamentais
- `regras-backend.md` — Java, Spring Boot, DDD
- `regras-frontend.md` — Angular, Signals, Standalone
- `regras-testes.md` — JUnit 5, Mockito, ArchUnit
