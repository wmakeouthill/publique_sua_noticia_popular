# 🎨 Diretrizes de Desenvolvimento Frontend

## Angular (major atual estável) • TypeScript • Clean Architecture

> **Este documento define os princípios, padrões e regras OBRIGATÓRIAS para desenvolvimento frontend.**
> Consulte `.cursorrules` para visão geral e `.cursorrules-backend` para regras do backend.

---

## 📋 ÍNDICE

1. [Stack Tecnológica](#1-stack-tecnológica)
2. [Princípios SOLID Aplicados ao Angular](#2-princípios-solid-aplicados-ao-angular)
3. [Arquitetura de Componentes](#3-arquitetura-de-componentes)
4. [Sintaxe Moderna Obrigatória](#4-sintaxe-moderna-obrigatória)
5. [Gerenciamento de Estado com Signals](#5-gerenciamento-de-estado-com-signals)
6. [Composables e Reutilização](#6-composables-e-reutilização)
7. [Organização de Código](#7-organização-de-código)
8. [Padrões de Nomenclatura](#8-padrões-de-nomenclatura)
9. [Clean Code no Frontend](#9-clean-code-no-frontend)
10. [Validações e Formulários](#10-validações-e-formulários)
11. [Testes](#11-testes)
12. [Anti-Patterns](#12-anti-patterns)
13. [Checklist de Qualidade](#13-checklist-de-qualidade)
14. [Acessibilidade (a11y)](#14-acessibilidade-a11y)
15. [Performance e Otimização](#15-performance-e-otimização)
16. [SSR e Hidratação](#16-ssr-e-hidratação-angular-universal)

---

## 1. STACK TECNOLÓGICA

### Versões Obrigatórias

| Tecnologia   | Versão      | Observação                           |
|--------------|-------------|--------------------------------------|
| Angular      | >= 20       | Standalone components obrigatório    |
| TypeScript   | >= 5.6      | Strict mode habilitado               |
| Node.js      | 22 LTS      | Toolchain/bundler                    |
| RxJS         | >= 7.8      | Uso reduzido (preferir Signals)      |

### Recursos Obrigatórios

```typescript
// ✅ SEMPRE usar estas features
inject()      // Injeção de dependência
signal()      // Estado reativo
computed()    // Valores derivados
input()       // Props de componentes
output()      // Eventos de componentes
effect()      // Efeitos colaterais

// ✅ SEMPRE usar esta sintaxe no HTML
@if, @else    // Condicionais
@for          // Loops
@switch       // Switch case
@defer        // Lazy loading
```

---

## 2. PRINCÍPIOS SOLID APLICADOS AO ANGULAR

### S - Single Responsibility (Responsabilidade Única)

```typescript
// ✅ CORRETO - Componente faz apenas uma coisa
@Component({ ... })
export class ProdutoCardComponent {
  readonly produto = input.required<Produto>();
  readonly onSelecionar = output<Produto>();
  // Apenas exibe e emite evento
}

// ❌ ERRADO - Múltiplas responsabilidades
@Component({ ... })
export class ProdutoComponent {
  // Lista, cria, edita, exclui, valida, formata...
}
```

### O - Open/Closed (Aberto/Fechado)

```typescript
// ✅ CORRETO - Extensível sem modificação
export interface ValidadorProduto {
  validar(produto: Produto): ValidationResult;
}

// Adicione novos validadores sem modificar existentes
export class ValidadorPreco implements ValidadorProduto { ... }
export class ValidadorEstoque implements ValidadorProduto { ... }
```

### L - Liskov Substitution (Substituição de Liskov)

```typescript
// ✅ CORRETO - Subtipos são substituíveis
abstract class BaseFormComponent<T> {
  abstract salvar(): void;
  abstract cancelar(): void;
}

export class ProdutoFormComponent extends BaseFormComponent<Produto> {
  salvar(): void { /* implementação */ }
  cancelar(): void { /* implementação */ }
}
```

### I - Interface Segregation (Segregação de Interfaces)

```typescript
// ✅ CORRETO - Interfaces pequenas e focadas
interface Listavel<T> {
  itens: Signal<T[]>;
  carregar(): void;
}

interface Paginavel {
  paginaAtual: Signal<number>;
  totalPaginas: Signal<number>;
  irParaPagina(pagina: number): void;
}

interface Filtravel<F> {
  filtro: Signal<F>;
  aplicarFiltro(filtro: F): void;
}

// ❌ ERRADO - Interface grande demais
interface CrudCompleto<T, F> {
  itens: Signal<T[]>;
  carregar(): void;
  criar(item: T): void;
  editar(item: T): void;
  excluir(id: string): void;
  filtrar(filtro: F): void;
  paginar(pagina: number): void;
  ordenar(campo: string): void;
  // ... mais 20 métodos
}
```

### D - Dependency Inversion (Inversão de Dependência)

```typescript
// ✅ CORRETO - Depende de abstrações
export class ProdutoListaComponent {
  private readonly produtoService = inject(ProdutoService); // Interface/Token
  // Não depende de implementação específica
}

// Service pode ser mockado facilmente para testes
```

---

## 3. ARQUITETURA DE COMPONENTES

### Estrutura de Pastas

```
src/app/
├── components/                    # Componentes de feature
│   └── {feature}/
│       ├── {feature}.component.ts
│       ├── {feature}.component.html
│       ├── {feature}.component.css
│       └── composables/           # Lógica reutilizável da feature
│           └── use-{nome}.ts
├── shared/                        # Componentes compartilhados
│   └── components/
├── services/                      # Serviços de aplicação
├── models/                        # Interfaces e tipos
├── utils/                         # Utilitários puros
├── guards/                        # Guards de rota
└── interceptors/                  # HTTP Interceptors
```

### Camadas por Feature (alinhado à Clean Architecture)

Para cada `{feature}`, mantenha a separação lógica abaixo (mesmo que a estrutura física varie):

- `pages/` = **smart/container** (coordena tela e estado)
- `components/` = **presentational** (UI pura)
- `services/` = **use cases/gateways** (HTTP, orquestração, composição)
- `models/` = **domínio/contratos** (tipos do negócio)
- `shared/` = **infra** (componentes reutilizáveis, pipes, interceptors)

Regra DDD-lite no frontend:

- ✅ `models` não devem “vazar” detalhes de transporte (ex.: campos/formatos do backend) sem um **adapter/mapper**.
- ✅ Quando necessário, crie `adapters/` ou funções de conversão (DRY) para mapear DTO ↔ model.

### Hierarquia de Componentes

```
┌─────────────────────────────────────────────┐
│  Smart Components (Container/Feature)        │
│  - Gerencia estado                          │
│  - Injeta services                          │
│  - Coordena componentes filhos              │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Dumb Components (Presentational)           │
│  - Recebe dados via input()                 │
│  - Emite eventos via output()               │
│  - Sem lógica de negócio                    │
│  - Altamente reutilizável                   │
└─────────────────────────────────────────────┘
```

---

## 4. SINTAXE MODERNA OBRIGATÓRIA

### 4.1 Injeção de Dependência

```typescript
// ✅ CORRETO - inject() moderno
export class ProdutoComponent {
  private readonly produtoService = inject(ProdutoService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
}

// ❌ PROIBIDO - Constructor injection
export class ProdutoComponent {
  constructor(
    private produtoService: ProdutoService,
    private router: Router
  ) {}
}
```

### 4.2 Input/Output Signals

```typescript
// ✅ CORRETO - Nova sintaxe signal-based
export class ProdutoCardComponent {
  // Inputs
  readonly produto = input.required<Produto>();
  readonly destacado = input<boolean>(false);
  readonly tamanho = input<'sm' | 'md' | 'lg'>('md');
  
  // Outputs
  readonly onSelecionar = output<Produto>();
  readonly onEditar = output<string>();
  readonly onExcluir = output<void>();
  
  // Computed baseado em input
  readonly precoFormatado = computed(() => 
    formatarMoeda(this.produto().preco)
  );
}

// ❌ PROIBIDO - Decorators antigos
export class ProdutoCardComponent {
  @Input() produto!: Produto;
  @Input() destacado = false;
  @Output() onSelecionar = new EventEmitter<Produto>();
}
```

### 4.3 Sintaxe de Template

```html
<!-- ✅ CORRETO - Nova sintaxe de controle -->
@if (carregando()) {
  <app-loading />
} @else if (erro()) {
  <app-erro [mensagem]="erro()" />
} @else {
  @for (produto of produtos(); track produto.id) {
    <app-produto-card 
      [produto]="produto" 
      (onSelecionar)="selecionarProduto($event)" 
    />
  } @empty {
    <p>Nenhum produto encontrado.</p>
  }
}

@switch (status()) {
  @case ('pendente') { <span class="badge-warning">Pendente</span> }
  @case ('aprovado') { <span class="badge-success">Aprovado</span> }
  @default { <span class="badge-secondary">Desconhecido</span> }
}

@defer (on viewport) {
  <app-componente-pesado />
} @placeholder {
  <app-skeleton />
} @loading (minimum 500ms) {
  <app-loading />
}

<!-- ❌ PROIBIDO - Sintaxe antiga -->
<div *ngIf="carregando">...</div>
<div *ngFor="let p of produtos">...</div>
<div [ngSwitch]="status">...</div>
```

### 4.4 Standalone Components

```typescript
// ✅ CORRETO - Standalone obrigatório
@Component({
  selector: 'app-produto-lista',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProdutoCardComponent,  // Importa apenas o necessário
    LoadingComponent
  ],
  templateUrl: './produto-lista.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProdutoListaComponent { }

// ❌ PROIBIDO - NgModules (exceto casos específicos de library)
@NgModule({
  declarations: [...],
  imports: [...],
  exports: [...]
})
export class ProdutoModule { }
```

---

## 5. GERENCIAMENTO DE ESTADO COM SIGNALS

### 5.1 Estado Local do Componente

```typescript
export class ProdutoListaComponent {
  // Estado primitivo
  readonly carregando = signal(false);
  readonly termoBusca = signal('');
  
  // Estado complexo
  readonly produtos = signal<Produto[]>([]);
  readonly produtoSelecionado = signal<Produto | null>(null);
  
  // Valores derivados (computados)
  readonly produtosFiltrados = computed(() => {
    const termo = this.termoBusca().toLowerCase();
    return this.produtos().filter(p => 
      p.nome.toLowerCase().includes(termo)
    );
  });
  
  readonly quantidadeProdutos = computed(() => 
    this.produtosFiltrados().length
  );
  
  readonly temProdutos = computed(() => 
    this.produtosFiltrados().length > 0
  );
}
```

### 5.2 Atualização de Estado

```typescript
// ✅ CORRETO - set() para substituição completa
this.produtos.set(novosProdutos);

// ✅ CORRETO - update() para mutações baseadas no estado anterior
this.produtos.update(lista => [...lista, novoProduto]);
this.produtos.update(lista => lista.filter(p => p.id !== id));
this.produtos.update(lista => 
  lista.map(p => p.id === produto.id ? produto : p)
);

// ❌ ERRADO - Mutação direta (não dispara reatividade)
this.produtos().push(novoProduto);
```

### 5.3 Effects para Efeitos Colaterais

```typescript
export class ProdutoComponent {
  readonly filtro = signal<FiltroType>({});
  
  constructor() {
    // ✅ CORRETO - Effect no contexto de injeção
    effect(() => {
      const filtroAtual = this.filtro();
      console.log('Filtro alterado:', filtroAtual);
      this.carregarProdutos(filtroAtual);
    });
  }
  
  // Para effects fora do constructor
  private readonly destroyRef = inject(DestroyRef);
  
  ngOnInit() {
    // Effect com cleanup
    effect((onCleanup) => {
      const subscription = this.algumObservable$.subscribe();
      onCleanup(() => subscription.unsubscribe());
    }, { injector: inject(Injector) });
  }
}
```

### 5.4 Quando NÃO Usar Signals

```typescript
// ✅ Use RxJS para:
// - Streams de eventos (WebSocket, Server-Sent Events)
// - Operações complexas de debounce/throttle
// - Combinação complexa de múltiplas streams assíncronas

// ✅ Use variáveis simples para:
// - Valores que nunca mudam
// - Configurações estáticas
readonly MAX_ITENS = 100;
readonly COLUNAS_TABELA = ['nome', 'preco', 'categoria'];
```

---

## 6. COMPOSABLES E REUTILIZAÇÃO

### 6.1 Padrão de Composable

```typescript
// src/app/components/gestao-caixa/composables/use-movimentacoes.ts

export function useMovimentacoes() {
  // Injeção de dependências
  const caixaService = inject(GestaoCaixaService);
  const notificationService = inject(NotificationService);
  
  // Estado encapsulado
  const movimentacoes = signal<Movimentacao[]>([]);
  const carregando = signal(false);
  const erro = signal<string | null>(null);
  
  // Computeds
  const totalEntradas = computed(() =>
    movimentacoes()
      .filter(m => m.tipo === 'ENTRADA')
      .reduce((acc, m) => acc + m.valor, 0)
  );
  
  const totalSaidas = computed(() =>
    movimentacoes()
      .filter(m => m.tipo === 'SAIDA')
      .reduce((acc, m) => acc + m.valor, 0)
  );
  
  const saldo = computed(() => totalEntradas() - totalSaidas());
  
  // Ações
  async function carregar(sessaoId: string): Promise<void> {
    carregando.set(true);
    erro.set(null);
    
    try {
      const dados = await firstValueFrom(
        caixaService.buscarMovimentacoes(sessaoId)
      );
      movimentacoes.set(dados);
    } catch (e) {
      erro.set('Erro ao carregar movimentações');
      notificationService.erro('Erro ao carregar movimentações');
    } finally {
      carregando.set(false);
    }
  }
  
  async function adicionar(movimentacao: CriarMovimentacao): Promise<boolean> {
    try {
      const nova = await firstValueFrom(
        caixaService.criarMovimentacao(movimentacao)
      );
      movimentacoes.update(lista => [...lista, nova]);
      notificationService.sucesso('Movimentação registrada');
      return true;
    } catch (e) {
      notificationService.erro('Erro ao registrar movimentação');
      return false;
    }
  }
  
  // Retorna API pública
  return {
    // Estado (readonly para consumidor)
    movimentacoes: movimentacoes.asReadonly(),
    carregando: carregando.asReadonly(),
    erro: erro.asReadonly(),
    totalEntradas,
    totalSaidas,
    saldo,
    
    // Ações
    carregar,
    adicionar
  };
}
```

### 6.2 Uso do Composable

```typescript
@Component({
  selector: 'app-gestao-caixa',
  standalone: true,
  // ...
})
export class GestaoCaixaComponent {
  private readonly movimentacoes = useMovimentacoes();
  
  // Expor para o template
  readonly listaMovimentacoes = this.movimentacoes.movimentacoes;
  readonly carregando = this.movimentacoes.carregando;
  readonly saldo = this.movimentacoes.saldo;
  
  constructor() {
    effect(() => {
      const sessaoId = this.sessaoAtual()?.id;
      if (sessaoId) {
        this.movimentacoes.carregar(sessaoId);
      }
    });
  }
  
  async registrarMovimentacao(dados: CriarMovimentacao): Promise<void> {
    await this.movimentacoes.adicionar(dados);
  }
}
```

### 6.3 Composables Utilitários

```typescript
// use-paginacao.ts
export function usePaginacao<T>(itens: Signal<T[]>, itensPorPagina = 10) {
  const paginaAtual = signal(1);
  
  const totalPaginas = computed(() => 
    Math.ceil(itens().length / itensPorPagina)
  );
  
  const itensPaginados = computed(() => {
    const inicio = (paginaAtual() - 1) * itensPorPagina;
    return itens().slice(inicio, inicio + itensPorPagina);
  });
  
  function irParaPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= totalPaginas()) {
      paginaAtual.set(pagina);
    }
  }
  
  return {
    paginaAtual: paginaAtual.asReadonly(),
    totalPaginas,
    itensPaginados,
    irParaPagina,
    proximaPagina: () => irParaPagina(paginaAtual() + 1),
    paginaAnterior: () => irParaPagina(paginaAtual() - 1)
  };
}

// use-debounce.ts
export function useDebounce<T>(valorInicial: T, delay = 300) {
  const valor = signal(valorInicial);
  const valorDebounced = signal(valorInicial);
  
  let timeoutId: ReturnType<typeof setTimeout>;
  
  effect(() => {
    const valorAtual = valor();
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      valorDebounced.set(valorAtual);
    }, delay);
  });
  
  return {
    valor,
    valorDebounced: valorDebounced.asReadonly()
  };
}
```

---

## 7. ORGANIZAÇÃO DE CÓDIGO

### 7.1 Ordem dos Membros do Componente

```typescript
@Component({ ... })
export class ExemploComponent {
  // 1. Injeções de dependência
  private readonly service = inject(ExemploService);
  private readonly router = inject(Router);
  
  // 2. Inputs
  readonly item = input.required<Item>();
  readonly modo = input<'edicao' | 'visualizacao'>('visualizacao');
  
  // 3. Outputs
  readonly onSalvar = output<Item>();
  readonly onCancelar = output<void>();
  
  // 4. Signals de estado
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  
  // 5. Computeds
  readonly itemValido = computed(() => this.validarItem(this.item()));
  readonly podeSalvar = computed(() => !this.carregando() && this.itemValido());
  
  // 6. Constructor com effects
  constructor() {
    effect(() => { /* ... */ });
  }
  
  // 7. Lifecycle hooks
  ngOnInit(): void { }
  ngOnDestroy(): void { }
  
  // 8. Métodos públicos (ações do template)
  salvar(): void { }
  cancelar(): void { }
  
  // 9. Métodos privados (lógica interna)
  private validarItem(item: Item): boolean { }
  private processarDados(): void { }
}
```

### 7.2 Organização de Arquivos por Feature

```
components/
└── gestao-caixa/
    ├── gestao-caixa.component.ts      # Container principal
    ├── gestao-caixa.component.html
    ├── gestao-caixa.component.css
    ├── composables/
    │   ├── use-movimentacoes.ts       # Lógica de movimentações
    │   ├── use-sessao-caixa.ts        # Lógica de sessão
    │   └── use-sugestoes-descricao.ts # Autocomplete
    ├── components/                     # Sub-componentes
    │   ├── movimentacao-form/
    │   ├── movimentacao-lista/
    │   └── resumo-caixa/
    └── models/                         # Tipos específicos da feature
        └── movimentacao.types.ts
```

---

## 8. PADRÕES DE NOMENCLATURA

### 8.1 Arquivos

| Tipo                  | Padrão                           | Exemplo                         |
|-----------------------|----------------------------------|---------------------------------|
| Componente            | `{nome}.component.ts`            | `produto-lista.component.ts`    |
| Service               | `{nome}.service.ts`              | `produto.service.ts`            |
| Composable            | `use-{nome}.ts`                  | `use-paginacao.ts`              |
| Model/Interface       | `{nome}.model.ts`                | `produto.model.ts`              |
| Utilitário            | `{nome}.util.ts`                 | `formato.util.ts`               |
| Guard                 | `{nome}.guard.ts`                | `auth.guard.ts`                 |
| Interceptor           | `{nome}.interceptor.ts`          | `error.interceptor.ts`          |

### 8.2 Classes e Interfaces

```typescript
// Classes: PascalCase + sufixo do tipo
export class ProdutoService { }
export class ProdutoListaComponent { }
export class AuthGuard { }

// Interfaces: PascalCase (sem prefixo I)
export interface Produto { }
export interface FiltrosProduto { }
export interface RespostaPaginada<T> { }

// Types: PascalCase + sufixo Type quando apropriado
export type StatusPedido = 'pendente' | 'preparando' | 'pronto';
export type ProdutoFormType = FormGroup<ProdutoFormControls>;
```

### 8.3 Variáveis e Funções

```typescript
// Signals: substantivos descritivos
readonly produtos = signal<Produto[]>([]);
readonly produtoSelecionado = signal<Produto | null>(null);
readonly carregandoProdutos = signal(false);

// Computeds: substantivos ou adjetivos
readonly produtosFiltrados = computed(() => ...);
readonly temProdutos = computed(() => ...);
readonly podeEditar = computed(() => ...);

// Funções/Métodos: verbos no infinitivo
function carregarProdutos(): void { }
function salvarProduto(produto: Produto): Promise<void> { }
function validarFormulario(): boolean { }

// Handlers de eventos: on + ação
function onProdutoSelecionado(produto: Produto): void { }
function onFormularioSubmit(): void { }

// ❌ PROIBIDO
let data: any;      // Nome genérico
let p: Produto;     // Abreviação
let handleClick();  // "handle" não é português
```

### 8.4 Constantes

```typescript
// Constantes: UPPER_SNAKE_CASE
export const MAX_UPLOAD_SIZE = 5 * 1024 * 1024;
export const API_BASE_URL = '/api';
export const DEBOUNCE_TIME_MS = 300;

// Enums: PascalCase para nome, UPPER_SNAKE_CASE para valores
export enum StatusPedido {
  PENDENTE = 'PENDENTE',
  EM_PREPARO = 'EM_PREPARO',
  PRONTO = 'PRONTO',
  ENTREGUE = 'ENTREGUE'
}
```

---

## 9. CLEAN CODE NO FRONTEND

### 9.1 Tamanho de Arquivos

| Tipo        | Máximo Recomendado | Limite Absoluto |
|-------------|-------------------|-----------------|
| Componente  | 150 linhas        | 300 linhas      |
| Composable  | 100 linhas        | 200 linhas      |
| Service     | 150 linhas        | 300 linhas      |
| Utilitário  | 100 linhas        | 200 linhas      |

### 9.2 Tamanho de Funções

```typescript
// ✅ CORRETO - Funções pequenas e focadas
async function carregarProdutos(): Promise<void> {
  carregando.set(true);
  try {
    const produtos = await buscarProdutosApi();
    processarProdutos(produtos);
  } finally {
    carregando.set(false);
  }
}

function processarProdutos(produtos: Produto[]): void {
  const validados = validarProdutos(produtos);
  const ordenados = ordenarPorNome(validados);
  this.produtos.set(ordenados);
}

// ❌ ERRADO - Função fazendo muitas coisas
async function carregarEProcessarEExibirProdutos(): Promise<void> {
  // 50 linhas de código fazendo tudo
}
```

### 9.3 DRY no Frontend

```typescript
// ✅ CORRETO - Reutilizar utilitários existentes
import { formatarMoeda, formatarData } from '../../utils/formato.util';
import { validarCPF, validarEmail } from '../../utils/validacao.util';

// ✅ CORRETO - Criar composables para lógica repetida
const { itens, carregar, adicionar } = useCrudGenerico<Produto>();

// ❌ ERRADO - Duplicar código
// Em ProdutoComponent:
formatarValor(valor: number): string {
  return 'R$ ' + valor.toFixed(2).replace('.', ',');
}
// Em PedidoComponent (mesmo código):
formatarPreco(preco: number): string {
  return 'R$ ' + preco.toFixed(2).replace('.', ',');
}
```

---

## 10. VALIDAÇÕES E FORMULÁRIOS

### 10.1 Formulários Tipados

```typescript
interface ProdutoForm {
  nome: FormControl<string>;
  preco: FormControl<number>;
  categoria: FormControl<string>;
  ativo: FormControl<boolean>;
}

export class ProdutoFormComponent {
  readonly form = new FormGroup<ProdutoForm>({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3)]
    }),
    preco: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0.01)]
    }),
    categoria: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    ativo: new FormControl(true, { nonNullable: true })
  });
  
  readonly formValido = computed(() => this.form.valid);
}
```

### 10.2 Validadores Customizados

```typescript
// src/app/utils/validadores.util.ts
export function validadorCPF(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const valido = validarCPF(control.value);
    return valido ? null : { cpfInvalido: true };
  };
}

export function validadorPrecoMinimo(minimo: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value < minimo) {
      return { precoMinimo: { minimo, atual: control.value } };
    }
    return null;
  };
}
```

### 10.3 Mensagens de Erro

```typescript
// Centralizar mensagens de erro
const MENSAGENS_ERRO: Record<string, string> = {
  required: 'Campo obrigatório',
  minlength: 'Mínimo de {requiredLength} caracteres',
  min: 'Valor mínimo: {min}',
  email: 'E-mail inválido',
  cpfInvalido: 'CPF inválido'
};

export function obterMensagemErro(errors: ValidationErrors | null): string {
  if (!errors) return '';
  const primeiroErro = Object.keys(errors)[0];
  const mensagem = MENSAGENS_ERRO[primeiroErro] || 'Campo inválido';
  return mensagem.replace(/{(\w+)}/g, (_, key) => errors[primeiroErro][key]);
}
```

---

## 11. TESTES

### 11.1 Estrutura de Testes

```typescript
describe('ProdutoListaComponent', () => {
  let component: ProdutoListaComponent;
  let produtoService: jasmine.SpyObj<ProdutoService>;
  
  beforeEach(async () => {
    produtoService = jasmine.createSpyObj('ProdutoService', ['buscarTodos']);
    
    await TestBed.configureTestingModule({
      imports: [ProdutoListaComponent],
      providers: [
        { provide: ProdutoService, useValue: produtoService }
      ]
    }).compileComponents();
    
    component = TestBed.createComponent(ProdutoListaComponent).componentInstance;
  });
  
  describe('carregamento inicial', () => {
    it('deve exibir loading enquanto carrega', () => {
      // Arrange
      produtoService.buscarTodos.and.returnValue(of([]).pipe(delay(100)));
      
      // Act
      component.ngOnInit();
      
      // Assert
      expect(component.carregando()).toBeTrue();
    });
    
    it('deve carregar e exibir produtos', fakeAsync(() => {
      // Arrange
      const produtos = [criarProdutoMock(), criarProdutoMock()];
      produtoService.buscarTodos.and.returnValue(of(produtos));
      
      // Act
      component.ngOnInit();
      tick();
      
      // Assert
      expect(component.produtos()).toEqual(produtos);
      expect(component.carregando()).toBeFalse();
    }));
  });
});
```

### 11.2 Testes de Composables

```typescript
describe('useMovimentacoes', () => {
  it('deve calcular saldo corretamente', () => {
    TestBed.runInInjectionContext(() => {
      const composable = useMovimentacoes();
      
      // Simular dados
      composable.movimentacoes.set([
        { tipo: 'ENTRADA', valor: 100 },
        { tipo: 'SAIDA', valor: 30 },
        { tipo: 'ENTRADA', valor: 50 }
      ]);
      
      expect(composable.totalEntradas()).toBe(150);
      expect(composable.totalSaidas()).toBe(30);
      expect(composable.saldo()).toBe(120);
    });
  });
});
```

---

## 12. ANTI-PATTERNS

### ❌ Proibições Absolutas

1. **Constructor injection** ao invés de `inject()`
2. **Decorators `@Input()` e `@Output()`** ao invés de `input()` e `output()`
3. **Diretivas `*ngIf`, `*ngFor`** ao invés de `@if`, `@for`
4. **NgModules** ao invés de standalone components
5. **BehaviorSubject** para estado simples (use Signals)
6. **`any` sem tipagem** adequada
7. **Componentes com mais de 300 linhas**
8. **Lógica de negócio em templates**
9. **Strings mágicas** espalhadas pelo código
10. **Mutação direta de signals** (`this.lista().push()`)

### ⚠️ Code Smells a Evitar

```typescript
// ❌ Múltiplas responsabilidades
export class GodComponent {
  // CRUD completo + validações + formatação + navegação
}

// ❌ Prop drilling excessivo
<app-a [dados]="dados">
  <app-b [dados]="dados">
    <app-c [dados]="dados">
      <app-d [dados]="dados" />
    </app-c>
  </app-b>
</app-a>

// ❌ Lógica complexa no template
@if (usuario && usuario.permissoes && usuario.permissoes.includes('admin') && !carregando) {
  ...
}

// ✅ CORRETO - Extrair para computed
readonly podeVerAdmin = computed(() => 
  this.usuario()?.permissoes?.includes('admin') && !this.carregando()
);
```

---

## 13. CHECKLIST DE QUALIDADE

### Antes de Criar um Componente

- [ ] Verificar se não existe componente similar
- [ ] Verificar se pode usar composable existente
- [ ] Definir se é Smart ou Dumb component
- [ ] Planejar inputs, outputs e estado

### Antes de Commitar

- [ ] Todos os componentes são standalone
- [ ] Usando `inject()` para DI
- [ ] Usando `signal()`, `computed()`, `input()`, `output()`
- [ ] Usando `@if`, `@for`, `@switch` no template
- [ ] ChangeDetection.OnPush habilitado
- [ ] Arquivo com menos de 300 linhas
- [ ] Funções com menos de 20 linhas
- [ ] Sem código duplicado
- [ ] Nomes descritivos e autoexplicativos
- [ ] Tipos bem definidos (sem `any`)
- [ ] Imports organizados e otimizados

---

## 14. ACESSIBILIDADE (a11y)

### 14.1 Princípios WCAG 2.2

| Princípio | Regra |
|-----------|-------|
| Perceptível | Todo conteúdo não-textual deve ter alternativa textual (`alt`, `aria-label`) |
| Operável | Toda funcionalidade deve ser acessível via teclado |
| Compreensível | Interface deve ser previsível e ajudar a evitar erros |
| Robusto | Conteúdo deve ser interpretável por tecnologias assistivas |

### 14.2 Regras Obrigatórias

```html
<!-- ✅ CORRETO — Imagens com alt significativo -->
<img [src]="produto.imagem" [alt]="produto.nome + ' - ' + produto.categoria" />

<!-- ✅ CORRETO — Botões com ícone precisam de aria-label -->
<button (click)="excluir(item)" aria-label="Excluir item {{ item.nome }}">
  <mat-icon>delete</mat-icon>
</button>

<!-- ✅ CORRETO — Formulários com labels associados -->
<label for="email">E-mail</label>
<input id="email" formControlName="email" type="email"
       aria-describedby="email-error" />
@if (form.controls.email.hasError('email')) {
  <span id="email-error" role="alert">E-mail inválido</span>
}

<!-- ✅ CORRETO — Headings hierárquicos (nunca pular níveis) -->
<h1>Cardápio</h1>
<h2>Lanches</h2>
<h3>Hamburgers</h3>

<!-- ✅ CORRETO — Live regions para conteúdo dinâmico -->
<div aria-live="polite" aria-atomic="true">
  @if (mensagem()) {
    <span>{{ mensagem() }}</span>
  }
</div>

<!-- ✅ CORRETO — Skip navigation link -->
<a class="skip-link" href="#main-content">Pular para conteúdo principal</a>
```

### 14.3 Navegação por Teclado

```typescript
// ✅ CORRETO — Focus management em modais/diálogos
@Component({
  selector: 'app-dialog',
  template: `
    <div role="dialog" aria-modal="true" aria-labelledby="dialog-title"
         (keydown.escape)="fechar()" cdkTrapFocus>
      <h2 id="dialog-title">{{ titulo() }}</h2>
      <ng-content />
    </div>
  `
})
export class DialogComponent {
  titulo = input.required<string>();
  private elementRef = inject(ElementRef);

  ngAfterViewInit() {
    // Foco automático no primeiro elemento focável
    const firstFocusable = this.elementRef.nativeElement
      .querySelector('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    firstFocusable?.focus();
  }
}
```

### 14.4 Checklist a11y

- [ ] Toda imagem tem `alt` descritivo (ou `alt=""` se decorativa)
- [ ] Todo controle interativo é acessível via teclado (Tab, Enter, Escape)
- [ ] Formulários têm labels associados (`for`/`id` ou `aria-label`)
- [ ] Erros de validação são anunciados (`role="alert"` ou `aria-live`)
- [ ] Contraste mínimo 4.5:1 para texto normal, 3:1 para texto grande
- [ ] Focus visible em todos os elementos interativos
- [ ] Headings em ordem hierárquica (h1 → h2 → h3)
- [ ] Sem `tabindex` > 0 (altera ordem natural)
- [ ] Usar `@angular/cdk/a11y` (`LiveAnnouncer`, `FocusTrap`, `FocusMonitor`)

---

## 15. PERFORMANCE E OTIMIZAÇÃO

### 15.1 Lazy Loading e Code Splitting

```typescript
// ✅ CORRETO — Lazy loading de rotas (sempre para feature modules)
export const routes: Routes = [
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent),
    canActivate: [authGuard]
  },
  {
    path: 'relatorios',
    loadChildren: () => import('./relatorios/relatorios.routes').then(m => m.RELATORIOS_ROUTES)
  }
];

// ✅ CORRETO — @defer para componentes pesados no template
@defer (on viewport) {
  <app-grafico-vendas [dados]="dadosVendas()" />
} @placeholder {
  <div class="skeleton-chart"></div>
} @loading (minimum 300ms) {
  <app-spinner />
}
```

### 15.2 Otimização de Change Detection

```typescript
// ✅ CORRETO — OnPush obrigatório em TODOS os componentes
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  // ...
})

// ✅ CORRETO — trackBy obrigatório em @for
@for (produto of produtos(); track produto.id) {
  <app-produto-card [produto]="produto" />
}

// ✅ CORRETO — computed() para derivações (evita recálculos)
total = computed(() =>
  this.itens().reduce((acc, item) => acc + item.preco * item.quantidade, 0)
);

// ✅ CORRETO — Unsubscribe automático com takeUntilDestroyed()
private destroyRef = inject(DestroyRef);
this.observable$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(/*...*/);
```

### 15.3 Bundle Size

| Técnica | Detalhe |
|---------|---------|
| Tree shaking | Importar apenas o necessário: `import { map } from 'rxjs/operators'` |
| Lazy loading | Toda feature em rota separada com `loadComponent/loadChildren` |
| `@defer` | Componentes pesados (gráficos, editores, mapas) com defer |
| Imagens | Usar `NgOptimizedImage` com `loading="lazy"` e formatos modernos (WebP/AVIF) |
| Fontes | `font-display: swap`, preload de fontes críticas |
| Análise | Usar `source-map-explorer` ou `webpack-bundle-analyzer` regularmente |

### 15.4 Caching e Requisições

```typescript
// ✅ CORRETO — Cache de requisições estáveis com shareReplay
@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);

  private categorias$ = this.http.get<Categoria[]>('/api/v1/categorias').pipe(
    shareReplay({ bufferSize: 1, refCount: true })
  );

  listar() { return this.categorias$; }
}

// ✅ CORRETO — Preloading strategy para rotas frequentes
@NgModule({
  imports: [RouterModule.forRoot(routes, {
    preloadingStrategy: PreloadAllModules  // ou QuicklinkStrategy
  })]
})
```

### 15.5 Regras de Performance

- ✅ **SEMPRE** use `ChangeDetectionStrategy.OnPush`
- ✅ **SEMPRE** use `track` em `@for` loops
- ✅ **SEMPRE** lazy load rotas de features
- ✅ **SEMPRE** use `@defer` para componentes pesados fora da viewport inicial
- ✅ **SEMPRE** use `NgOptimizedImage` para imagens
- ❌ **NUNCA** importe módulos inteiros quando só precisa de partes
- ❌ **NUNCA** faça chamadas HTTP em `computed()` ou `effect()`
- ❌ **NUNCA** use `Default` change detection

---

## 16. SSR E HIDRATAÇÃO (Angular Universal)

### ⚠️ REGRAS CRÍTICAS PARA EVITAR ERROS DE HIDRATAÇÃO (NG0506)

O Angular SSR (Server-Side Rendering) renderiza a aplicação no servidor antes de enviar ao browser.
A **hidratação** é o processo de "ativar" o HTML estático no cliente.

#### Erros Comuns e Como Evitar

**Erro NG0506:** `ApplicationRef.isStable() não emitiu true dentro de 10000ms`

Este erro ocorre quando há operações assíncronas pendentes que impedem a estabilização da aplicação.

#### Causas Principais

1. **Requisições HTTP no servidor** - Subscriptions que nunca completam
2. **setInterval/setTimeout** - Timers que não são limpos
3. **Observables infinitos** - Streams que nunca completam
4. **WebSocket/EventSource** - Conexões persistentes

#### Regras Obrigatórias

```typescript
// ✅ CORRETO - Verificar se está no browser antes de fazer requisições HTTP em ngOnInit
@Component({ ... })
export class MeuComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  ngOnInit(): void {
    // Requisições HTTP apenas no browser
    if (this.isBrowser) {
      this.carregarDados();
    }
  }
}

// ✅ CORRETO - Composables que fazem HTTP devem ser chamados apenas no browser
export function useMinhaFeature() {
  // Funções que fazem HTTP não devem ser chamadas automaticamente
  // O componente que usa o composable deve verificar isBrowser
  function carregarDados(): void {
    service.buscarDados().subscribe({ ... });
  }

  return { carregarDados, ... };
}

// No componente:
ngOnInit(): void {
  if (this.isBrowser) {
    this.meuComposable.carregarDados();
  }
}

// ✅ CORRETO - Usar @defer para lazy loading de conteúdo pesado
@defer (on viewport) {
  <componente-pesado />
} @placeholder {
  <div class="skeleton"></div>
}

// ❌ ERRADO - Fazer requisição HTTP no servidor sem controle
ngOnInit(): void {
  this.http.get('/api/dados').subscribe(); // Pode travar hidratação!
}

// ❌ ERRADO - Criar interval/timeout sem limpar
ngOnInit(): void {
  setInterval(() => { ... }, 1000); // Nunca para!
}
```

#### Padrão para Modais com Dados Assíncronos

```typescript
// ✅ CORRETO - Carregar dados apenas quando modal é aberto (ação do usuário)
function abrirModal(): void {
  mostrarModal.set(true);
  carregarDados(); // OK: ação do usuário, já está no browser
}

// ✅ CORRETO - Não carregar dados automaticamente em composables
export function useEstatisticas() {
  // NÃO chame carregarEstatisticas() automaticamente aqui
  // Deixe o componente decidir quando carregar

  function abrirModal(): void {
    mostrarModal.set(true);
    carregarEstatisticas(); // Carrega apenas quando usuário abre
  }

  return { abrirModal, ... };
}
```

#### 🔥 REGRA CRÍTICA: Timers e Polling com NgZone.runOutsideAngular

**PROBLEMA:** Timers (`setInterval`, `timer()`) que nunca completam impedem `ApplicationRef.isStable()`
de emitir `true`, causando o erro NG0506 de timeout de hidratação.

**SOLUÇÃO:** Use `NgZone.runOutsideAngular()` para executar timers fora da zona Angular,
e `NgZone.run()` para atualizar estado dentro da zona (trigger change detection).

```typescript
// ✅ CORRETO - Timer/Polling fora da zona Angular
@Injectable({ providedIn: 'root' })
export class MeuPollingService {
  private readonly ngZone = inject(NgZone);
  private readonly http = inject(HttpClient);
  
  readonly dados = signal<Dados[]>([]);
  private subscription: Subscription | null = null;

  iniciarPolling(): void {
    // Executa o timer FORA da zona Angular
    this.ngZone.runOutsideAngular(() => {
      this.subscription = timer(0, 5000).pipe(
        switchMap(() => this.http.get<Dados[]>('/api/dados'))
      ).subscribe(resultado => {
        // Atualiza estado DENTRO da zona para trigger change detection
        this.ngZone.run(() => this.dados.set(resultado));
      });
    });
  }

  pararPolling(): void {
    this.subscription?.unsubscribe();
  }
}

// ✅ CORRETO - setInterval em componentes fora da zona
@Component({ ... })
export class MeuComponent implements OnDestroy {
  private readonly ngZone = inject(NgZone);
  private intervalId: any = null;

  iniciar(): void {
    this.ngZone.runOutsideAngular(() => {
      this.intervalId = setInterval(() => {
        this.ngZone.run(() => this.atualizarEstado());
      }, 3000);
    });
  }

  ngOnDestroy(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }
}

// ❌ ERRADO - Timer dentro da zona Angular (bloqueia estabilidade!)
iniciarPolling(): void {
  timer(0, 5000).pipe(
    switchMap(() => this.http.get('/api/dados'))
  ).subscribe(resultado => {
    this.dados.set(resultado); // ⚠️ Nunca estabiliza!
  });
}

// ❌ ERRADO - setInterval sem NgZone (bloqueia estabilidade!)
ngOnInit(): void {
  setInterval(() => this.refresh(), 3000); // ⚠️ Erro NG0506!
}
```

**Quando usar `runOutsideAngular()`:**

- `setInterval()` / `clearInterval()`
- `timer()` / `interval()` do RxJS que não completam
- WebSocket connections
- Polling de APIs
- Animações contínuas com `requestAnimationFrame`

**Quando NÃO usar:**

- Requisições HTTP únicas (completam naturalmente)
- `setTimeout` curtos (< 5 segundos)
- Observables que completam após emitir

#### Checklist SSR

- [ ] Verificar `isPlatformBrowser()` antes de requisições HTTP em `ngOnInit`
- [ ] Nunca fazer requisições HTTP automáticas em construtores/composables
- [ ] **Usar `NgZone.runOutsideAngular()` para timers/polling que não completam**
- [ ] **Usar `NgZone.run()` dentro do timer para atualizar estado/signals**
- [ ] Usar `@defer` para componentes pesados
- [ ] Limpar timers e subscriptions no `ngOnDestroy`
- [ ] Modais devem carregar dados apenas quando abertos (ação do usuário)
- [ ] Evitar uso de `document`, `window`, `localStorage` sem verificar browser

---

## 📚 RECURSOS DO PROJETO

### Utilitários Disponíveis

```typescript
// src/app/utils/
import { formatarMoeda, formatarData } from './formato.util';
import { validarCPF, validarEmail } from './validacao.util';
import { paginar, ordenar } from './paginacao.util';
import { gerarChartConfig } from './chart.util';
import { filtrarPorTermo } from './filtro.util';
```

### Composables Existentes

Verifique em `components/{feature}/composables/` antes de criar novos.

---

**🚨 LEMBRE-SE: Código Angular moderno é declarativo, reativo e altamente tipado!**
