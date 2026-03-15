# 🎯 Diretrizes de Desenvolvimento (Cursor Rules) — Banco / SELIC

Este documento serve como **ponto de entrada** para os padrões de desenvolvimento.
As regras específicas estão organizadas em arquivos dedicados:

| Arquivo | Escopo | Descrição |
|---------|--------|-----------|
| `.cursorrules` | Geral | Princípios fundamentais e visão geral (este arquivo) |
| `.cursorrules-frontend` | Angular (major atual estável) | Regras específicas para desenvolvimento frontend |
| `.cursorrules-backend` | Java (LTS) / Spring Boot 3.x | Regras específicas para desenvolvimento backend |

---

## ⚠️ PRINCÍPIOS FUNDAMENTAIS INEGOCIÁVEIS (NÃO REMOVER)

### 1. Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE                          │
│         Frameworks, UI, DB, APIs Externas                    │
│                                                              │
│    ┌───────────────────────────────────────────────────┐    │
│    │                   APPLICATION                      │    │
│    │            Use Cases, DTOs, Ports                  │    │
│    │                                                    │    │
│    │    ┌───────────────────────────────────────┐      │    │
│    │    │              DOMAIN                    │      │    │
│    │    │    Entidades, Value Objects,           │      │    │
│    │    │    Regras de Negócio                   │      │    │
│    │    │    ❌ ZERO dependências externas       │      │    │
│    │    └───────────────────────────────────────┘      │    │
│    │                                                    │    │
│    └───────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

**Regra de Dependência:** `Infrastructure → Application → Domain`

❌ **NUNCA** inverta essa ordem!

---

### 2. SOLID

| Princípio | Aplicação | Regra |
|-----------|-----------|-------|
| **S**ingle Responsibility | Classes/Componentes | Uma classe = uma responsabilidade |
| **O**pen/Closed | Extensibilidade | Aberto para extensão, fechado para modificação |
| **L**iskov Substitution | Hierarquias | Subtipos devem ser substituíveis |
| **I**nterface Segregation | Interfaces | Interfaces pequenas e específicas |
| **D**ependency Inversion | Acoplamento | Dependa de abstrações, não de implementações |

---

### 3. DRY (Don't Repeat Yourself)

- ✅ Sempre verificar antes de criar:
  - Backend → bibliotecas comuns / módulos compartilhados
  - Frontend → `src/app/shared`, `src/app/utils` e composables
- ✅ Sempre centralizar conversões e mapeamentos:
  - Backend → mappers via DI (Spring) + JacksonConfig / MapStruct
  - Frontend → composables/facades/adapters reutilizáveis
- ✅ Sempre reutilizar antes de duplicar
- ❌ Nunca duplicar código existente
- ❌ Nunca criar utilitários sem verificar se já existem

### 3.1 Reutilização de mappers (JacksonConfig / Factories) — sem memory leak

- ❌ Proibido `new ObjectMapper()` espalhado no código.
- ❌ Proibido singleton manual/registry global (`getInstance()`, `static final` com cache de tipos).
- ✅ `ObjectMapper` deve ser configurado em **um único ponto** (JacksonConfig) e injetado.
- ✅ Mappers devem ser **stateless** e providos por DI (singleton controlado pelo container).
- ✅ Quando precisar de variações (views, features, custom modules): expor via **factories** (ex.: `ObjectReaderFactory`, `ObjectWriterFactory`) derivadas do `ObjectMapper` configurado.

---

### 4. Clean Code

#### Limites de Tamanho

| Escopo | Limite Recomendado | Limite Máximo |
|--------|-------------------|---------------|
| Classe/Componente | 150 linhas | 300 linhas |
| Método/Função | 15 linhas | 20 linhas |
| Parâmetros | 3 parâmetros | 5 parâmetros |

#### Nomenclatura

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Classes | PascalCase | `ProdutoService`, `CriarPedidoUseCase` |
| Métodos/Funções | camelCase (verbos) | `buscarProduto()`, `salvarCliente()` |
| Variáveis | camelCase | `produtoSelecionado`, `listaItens` |
| Constantes | UPPER_SNAKE_CASE | `MAX_UPLOAD_SIZE`, `API_URL` |
| Arquivos Angular | kebab-case | `produto-lista.component.ts` |

#### Proibições Absolutas

```
❌ Abreviações: prod, cat, svc, usr
❌ Nomes genéricos: data, info, util, helper, manager
❌ Código comentado no repositório
❌ Catch vazio ou genérico demais
❌ Variáveis com nomes de uma letra
```

---

## 🛠️ STACK TECNOLÓGICA

### Frontend

| Tecnologia | Versão | Política |
|------------|--------|----------|
| Angular | >= 20 | Preferir a major mais recente estável suportada; standalone por padrão |
| TypeScript | >= 5.6 | `strict` habilitado |
| Node.js | 22 LTS | Padrão para builds e toolchain |
| RxJS | >= 7.8 | Uso reduzido (preferir Signals) |

**Sintaxe Moderna OBRIGATÓRIA:**

```typescript
// Injeção e Estado
inject()      // DI
signal()      // Estado reativo
computed()    // Valores derivados
input()       // Props
output()      // Eventos
effect()      // Efeitos colaterais

// Templates
@if, @else, @for, @switch, @defer
```

📖 **Detalhes completos:** `.cursorrules-frontend`

### Backend

| Tecnologia | Versão | Política |
|------------|--------|----------|
| Java | 25 (LTS) | Preferido; 21 (LTS) permitido quando necessário |
| Spring Boot | 3.x | Usar a última minor estável homologada |
| Maven | 3.9+ | Build |
| Liquibase | 4.x | Migrações |
| Banco | Oracle / PostgreSQL / MySQL | Seguir o que o produto define; respeitar ACID |
| Lombok | Latest | Boilerplate (usar com parcimônia) |

**Padrões OBRIGATÓRIOS:**

```java
// Lombok
@RequiredArgsConstructor  // SEMPRE para injeção

// Mappers/Jackson
// ✅ Injetar mappers/factories via DI (singleton controlado pelo Spring)
// ❌ Proibido singleton manual (static getInstance / registry)

// Java (LTS)
record, sealed, text blocks, pattern matching (conforme versão do projeto)
```

📖 **Detalhes completos:** `.cursorrules-backend`

---

## 🧱 ARQUITETURA (visão unificada)

### Backend (DDD + Clean Architecture)

- Estrutura canônica por camadas: `dominio/` → `aplicacao/` → `infraestrutura/` e `interfaces/`.
- `interfaces/rest/v{n}/` define **contratos** (`*API.java`) e `controller/` implementa.
- O domínio contém regras de negócio e **não depende** de framework.

### Frontend (Clean Architecture adaptada)

- Organização por **feature**.
- `pages/` (smart/container), `components/` (presentational), `services/` (use cases/gateway HTTP), `models/` (domínio/contratos), `shared/` (infra).

## 📚 PRINCÍPIOS ADICIONAIS (OBRIGATÓRIOS QUANDO APLICÁVEL)

### DDD (Domain-Driven Design)

- Entidades ricas com invariantes.
- Value Objects imutáveis.
- Use Cases na aplicação (orquestração), não no controller.
- Ports/Adapters para integrações externas.

### ACID (Persistência)

- **Atomicidade**: mudanças relacionadas na mesma transação.
- **Consistência**: invariantes e constraints respeitadas.
- **Isolamento**: escolha consciente; evitar suposições.
- **Durabilidade**: commit + migrações versionadas.

### 5. KISS (Keep It Simple, Stupid)

- ✅ Preferir soluções simples e diretas
- ✅ Evitar over-engineering e abstrações desnecessárias
- ❌ Nunca adicionar complexidade "para o futuro" sem demanda real

### 6. YAGNI (You Ain't Gonna Need It)

- ✅ Implementar apenas o que é necessário agora
- ✅ Refatorar quando a necessidade surgir
- ❌ Nunca criar interfaces/abstrações "porque talvez precise"

### 7. Fail Fast

- ✅ Validar entradas o mais cedo possível
- ✅ Lançar exceções claras e específicas imediatamente
- ❌ Nunca propagar dados inválidos silenciosamente entre camadas

### 8. Composition over Inheritance

- ✅ Preferir composição (injeção, delegação) sobre herança
- ✅ Usar interfaces/traits para polimorfismo
- ❌ Evitar hierarquias profundas de herança (máximo 2 níveis)

### 9. Law of Demeter (Princípio do Menor Conhecimento)

- ✅ Objetos devem interagir apenas com colaboradores diretos
- ❌ Proibido cadeias como `pedido.getCliente().getEndereco().getCidade()`
- ✅ Expor métodos de conveniência: `pedido.getCidadeCliente()`

### 10. Tell, Don't Ask

- ✅ Pedir ao objeto que execute a ação, não consultar estado para decidir fora
- ✅ Entidades ricas que encapsulam comportamento
- ❌ Nunca extrair estado de entidade para lógica externa

### Patterns (evitar reinventar roda)

- Backend: Factory, Strategy, Adapter, Facade, Repository, Specification, Builder, Observer/Events, CQRS (quando fizer sentido).
- Frontend: Facade, shared modules, Adapter, Container/Presentational, Composition (composables), Compound Components.

---

## ✅ CHECKLIST UNIVERSAL

### Antes de Iniciar uma Feature

- [ ] Li e entendi a arquitetura do módulo
- [ ] Verifiquei se existe código similar que pode ser reutilizado
- [ ] Defini as camadas onde o código será criado
- [ ] Planejei a divisão de responsabilidades

### Antes de Commitar

#### Arquitetura

- [ ] Clean Architecture respeitada (dependências corretas)
- [ ] Domain sem dependências de frameworks
- [ ] Responsabilidade única por classe/componente

#### Qualidade de Código

- [ ] Arquivos com menos de 300 linhas
- [ ] Funções/métodos com menos de 20 linhas
- [ ] Nomes descritivos e autoexplicativos
- [ ] Sem código duplicado

#### Frontend (Angular)

- [ ] Usando `inject()`, não constructor injection
- [ ] Usando `signal()`, `computed()`, `input()`, `output()`
- [ ] Usando `@if`, `@for`, `@switch` nos templates
- [ ] Standalone components apenas
- [ ] ChangeDetection.OnPush habilitado

#### Backend (Java)

- [ ] Usando `@RequiredArgsConstructor`
- [ ] Usando mappers/factories via DI (JacksonConfig / MapStruct), sem singleton manual
- [ ] Entidades ricas (com comportamento)
- [ ] Value Objects imutáveis
- [ ] Exceções específicas e tratadas

#### Testes

- [ ] Testes unitários para lógica de negócio
- [ ] Testes de casos de erro
- [ ] Padrão AAA (Arrange, Act, Assert)

---

## 🚫 ANTI-PATTERNS UNIVERSAIS

### Código

1. ❌ **God Classes** - Classes fazendo muitas coisas
2. ❌ **Entidades Anêmicas** - Apenas getters/setters, sem comportamento
3. ❌ **Código Duplicado** - Violar DRY
4. ❌ **Nomes Genéricos** - `data`, `info`, `util`, `helper`
5. ❌ **Abreviações** - `prod`, `cat`, `svc`, `usr`
6. ❌ **Magic Numbers/Strings** - Valores hardcoded sem constantes

### Arquitetura

1. ❌ **Dependências Invertidas** - Domain dependendo de Infrastructure
2. ❌ **Lógica no Controller** - Regras de negócio na camada web
3. ❌ **Framework no Domain** - JPA, Spring annotations no domínio
4. ❌ **Use Cases Gigantes** - Casos de uso fazendo muitas coisas

### Frontend Específico

1. ❌ **Sintaxe Antiga** - `*ngIf`, `*ngFor`, `@Input()`, `@Output()`
2. ❌ **Constructor Injection** - Ao invés de `inject()`
3. ❌ **BehaviorSubject para Estado** - Ao invés de Signals
4. ❌ **NgModules** - Ao invés de Standalone Components

### Backend Específico

1. ❌ **Construtor Manual** - Ao invés de `@RequiredArgsConstructor`
2. ❌ **Singleton manual/registry global** (ex.: `getInstance()`, `static` cache, `new ObjectMapper()` espalhado)
3. ❌ **Retornar `null`** - Ao invés de `Optional`
4. ❌ **Catch Vazio** - Engolir exceções

---

## 📚 REFERÊNCIA RÁPIDA

### Criar Novo Componente Angular

```typescript
// ✅ Template correto
@Component({
  selector: 'app-exemplo',
  standalone: true,
  imports: [CommonModule, /* apenas o necessário */],
  templateUrl: './exemplo.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExemploComponent {
  // 1. Injeções
  private readonly service = inject(ExemploService);
  
  // 2. Inputs/Outputs
  readonly item = input.required<Item>();
  readonly onSalvar = output<Item>();
  
  // 3. State
  readonly carregando = signal(false);
  
  // 4. Computeds
  readonly itemValido = computed(() => this.validar(this.item()));
  
  // 5. Methods
  salvar(): void { }
}
```

### Criar Novo Use Case Java

```java
// ✅ Template correto
@Service
@RequiredArgsConstructor
public class CriarExemploUseCase {
    
    private final ExemploRepositoryPort repository;
    private final ValidadorService validador;
    private final ExemploMapper exemploMapper;
    
    @Transactional
    public ExemploDTO executar(CriarExemploRequest request) {
        validarRequest(request);
        
        Exemplo exemplo = Exemplo.criar(request.nome());
        Exemplo salvo = repository.salvar(exemplo);
        
        return exemploMapper.toDto(salvo);
    }
    
    private void validarRequest(CriarExemploRequest request) {
        // Validação de entrada
    }
}
```

---

## 🔗 LINKS PARA DOCUMENTAÇÃO DETALHADA

| Documento | Conteúdo |
|-----------|----------|
| `.cursorrules-frontend` | Regras Angular, Signals, Composables, Testes Frontend |
| `.cursorrules-backend` | Regras Java, Clean Architecture, DDD, Testes Backend |
| `rules.md` | Índice e referência rápida do repositório |

---

**🚨 ESTAS DIRETRIZES SÃO INEGOCIÁVEIS**

Sempre siga todas as regras antes de implementar qualquer funcionalidade.
Em caso de dúvida, consulte os arquivos específicos de frontend ou backend.
