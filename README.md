# 📰 Portal de Notícias Populares

O **Portal de Notícias Populares** é uma aplicação completa (Full-Stack) voltada para a criação e leitura de notícias de forma rápida e moderna. O sistema possui arquitetura limpa e integra-se com inteligência artificial (Gemini) para acelerar a produção de conteúdo.

## 🛠️ Tecnologias Principais

- **Backend:** Java 21, Spring Boot 3.4, Clean Architecture
- **Frontend:** Angular 21, Standalone Components, Signals, UI Glassmorphism (Dark Mode)
- **Banco de Dados:** PostgreSQL 16
- **IA e Autenticação:** Gemini API, Google OAuth2
- **Infraestrutura:** Docker e Docker Compose

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

- **Frontend (UI):** [http://localhost:4200](http://localhost:4200)
- **Backend (API):** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **Health Check do Backend:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 6. Parar a Aplicação
Quando terminar, você pode desligar tudo rodando o seguinte comando no PowerShell:

```powershell
# Desliga os contêineres e preserva os volumes do banco
docker-compose down

# Caso queira desligar e DELETAR os dados do banco (Zerar o DB)
docker-compose down -v
```

---

## 📁 Estrutura do Repositório

```text
publique_sua_noticia_popular/
├── backend/          # API Java 21 com Spring Boot (Porta 8080)
├── frontend/         # Aplicação Angular 21 (Porta 4200)
├── docker-compose.yml# Orquestrador dos contêineres
├── .env              # Variáveis de ambiente
└── README.md         # Documentação atual
```
