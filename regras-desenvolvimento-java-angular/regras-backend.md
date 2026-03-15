# ☕ Diretrizes de Desenvolvimento Backend

## Java (LTS) • Spring Boot 3.x • Clean Architecture • DDD

> **Este documento define os princípios, padrões e regras OBRIGATÓRIAS para desenvolvimento backend.**
> Consulte `.cursorrules` para visão geral e `.cursorrules-frontend` para regras do frontend.

---

## 📋 ÍNDICE

1. [Stack Tecnológica](#1-stack-tecnológica)
2. [Clean Architecture](#2-clean-architecture)
3. [Domain-Driven Design (DDD)](#3-domain-driven-design-ddd)
4. [Princípios SOLID](#4-princípios-solid)
5. [Estrutura de Pacotes](#5-estrutura-de-pacotes)
6. [Camada Domain](#6-camada-domain)
7. [Camada Application](#7-camada-application)
8. [Camada Infrastructure](#8-camada-infrastructure)
9. [Lombok e Java Moderno (21/25 LTS)](#9-lombok-e-java-moderno-21--25-lts)
10. [Mappers e Conversões](#10-mappers-e-conversões)
11. [Persistência e Repositórios](#11-persistência-e-repositórios)
12. [Validações e Exceções](#12-validações-e-exceções)
13. [Migrações com Liquibase](#13-migrações-com-liquibase)
14. [Segurança](#14-segurança)
15. [Logging e Observabilidade](#15-logging-e-observabilidade)
16. [API Versioning e Boas Práticas REST](#16-api-versioning-e-boas-práticas-rest)
17. [Testes](#17-testes)
18. [Anti-Patterns](#18-anti-patterns)
19. [Checklist de Qualidade](#19-checklist-de-qualidade)

---

## 1. STACK TECNOLÓGICA

### Versões Obrigatórias

| Tecnologia   | Versão      | Observação                           |
|--------------|-------------|--------------------------------------|
| Java         | 25 (LTS)    | Preferido; 21 (LTS) permitido quando necessário |
| Spring Boot  | 3.x         | Usar a última minor estável homologada |
| Liquibase    | 4.x         | Migrações de banco                   |
| Maven        | 3.9+        | Build tool padrão                    |
| Banco        | Oracle / PostgreSQL / MySQL | Seguir o que o produto define; respeitar ACID |
| Lombok       | Latest      | Redução de boilerplate               |

### Dependências Compartilhadas

```xml
<!-- Sempre usar kernel-compartilhado -->
<dependency>
    <groupId>com.snackbar</groupId>
    <artifactId>kernel-compartilhado</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 2. CLEAN ARCHITECTURE

### 2.1 Visão Geral

```
┌─────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE                          │
│  Controllers, Repositories, External Services, Config        │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                     APPLICATION                        │  │
│  │       Use Cases, DTOs, Application Services            │  │
│  │                                                        │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │                    DOMAIN                        │  │  │
│  │  │    Entities, Value Objects, Domain Services      │  │  │
│  │  │         ❌ ZERO dependências externas            │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                        │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Regra de Dependência (INVIOLÁVEL)

```
Infrastructure  →  Application  →  Domain
     ↓                  ↓             ↓
  Frameworks         Ports        Entidades
  BD, Web, etc.    Interfaces    Value Objects
                   Use Cases     Regras Negócio
```

✅ **Camadas externas DEPENDEM de camadas internas**
❌ **Camadas internas NUNCA dependem de camadas externas**

### 2.3 Regras por Camada

| Camada         | Pode Depender De        | NÃO Pode Depender De        |
|----------------|------------------------|-----------------------------|
| Domain         | Nada (é o núcleo)      | Application, Infrastructure |
| Application    | Domain                 | Infrastructure              |
| Infrastructure | Application, Domain    | -                           |

---

## 3. DOMAIN-DRIVEN DESIGN (DDD)

### 3.1 Building Blocks

```
Domain/
├── entities/           # Entidades com identidade
├── valueobjects/       # Objetos imutáveis sem identidade
├── aggregates/         # Clusters de entidades (se aplicável)
├── services/           # Lógica de domínio entre entidades
├── events/             # Eventos de domínio
└── exceptions/         # Exceções de domínio
```

### 3.2 Entidades

```java
// ✅ CORRETO - Entidade rica com comportamento
public class Pedido {
    private final String id;
    private final List<ItemPedido> itens;
    private StatusPedido status;
    private final Instant criadoEm;
    
    // Construtor privado - use factory method
    private Pedido(String id, List<ItemPedido> itens) {
        this.id = id;
        this.itens = new ArrayList<>(itens);
        this.status = StatusPedido.PENDENTE;
        this.criadoEm = Instant.now();
    }
    
    // Factory method - único ponto de criação
    public static Pedido criar(List<ItemPedido> itens) {
        validarItens(itens);
        return new Pedido(UUID.randomUUID().toString(), itens);
    }
    
    // Comportamento de negócio na entidade
    public void confirmar() {
        if (this.status != StatusPedido.PENDENTE) {
            throw new EstadoInvalidoException("Pedido não pode ser confirmado");
        }
        this.status = StatusPedido.CONFIRMADO;
    }
    
    public void adicionarItem(ItemPedido item) {
        validarPodeModificar();
        this.itens.add(item);
    }
    
    public BigDecimal calcularTotal() {
        return itens.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Validação de invariantes
    private static void validarItens(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("Pedido deve ter ao menos um item");
        }
    }
    
    private void validarPodeModificar() {
        if (this.status != StatusPedido.PENDENTE) {
            throw new EstadoInvalidoException("Pedido não pode ser modificado");
        }
    }
    
    // Getters - NUNCA setters públicos
    public String getId() { return id; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public StatusPedido getStatus() { return status; }
}

// ❌ ERRADO - Entidade anêmica (apenas dados)
public class Pedido {
    private String id;
    private List<ItemPedido> itens;
    private StatusPedido status;
    
    // Getters e setters...
    public void setStatus(StatusPedido status) { this.status = status; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
}
```

### 3.3 Value Objects

```java
// ✅ CORRETO - Value Object imutável com validação
public record Preco(BigDecimal valor) {
    
    public Preco {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("Preço deve ser positivo");
        }
    }
    
    public static Preco of(BigDecimal valor) {
        return new Preco(valor);
    }
    
    public static Preco zero() {
        return new Preco(BigDecimal.ZERO);
    }
    
    public Preco somar(Preco outro) {
        return new Preco(this.valor.add(outro.valor));
    }
    
    public Preco multiplicar(int quantidade) {
        return new Preco(this.valor.multiply(BigDecimal.valueOf(quantidade)));
    }
    
    public boolean maiorQue(Preco outro) {
        return this.valor.compareTo(outro.valor) > 0;
    }
}

// ✅ CORRETO - Value Object para CPF
public record CPF(String valor) {
    
    private static final Pattern PATTERN = Pattern.compile("\\d{11}");
    
    public CPF {
        String limpo = valor.replaceAll("[^0-9]", "");
        if (!PATTERN.matcher(limpo).matches() || !validarDigitos(limpo)) {
            throw new CPFInvalidoException(valor);
        }
        valor = limpo; // Armazena apenas números
    }
    
    public static CPF of(String valor) {
        return new CPF(valor);
    }
    
    public String formatado() {
        return valor.substring(0, 3) + "." + 
               valor.substring(3, 6) + "." + 
               valor.substring(6, 9) + "-" + 
               valor.substring(9);
    }
    
    private static boolean validarDigitos(String cpf) {
        // Lógica de validação de dígitos verificadores
    }
}
```

### 3.4 Domain Services

```java
// ✅ CORRETO - Lógica que não pertence a uma entidade específica
public class CalculadoraDescontoService {
    
    public BigDecimal calcularDesconto(Pedido pedido, Cliente cliente) {
        BigDecimal total = pedido.calcularTotal();
        BigDecimal desconto = BigDecimal.ZERO;
        
        // Desconto por fidelidade
        if (cliente.ehFidelidade()) {
            desconto = desconto.add(total.multiply(new BigDecimal("0.05")));
        }
        
        // Desconto por valor
        if (total.compareTo(new BigDecimal("100")) > 0) {
            desconto = desconto.add(total.multiply(new BigDecimal("0.10")));
        }
        
        return desconto;
    }
}
```

---

## 4. PRINCÍPIOS SOLID

### S - Single Responsibility

```java
// ✅ CORRETO - Uma responsabilidade por classe
@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {
    private final PedidoRepositoryPort repository;
    
    public PedidoDTO executar(CriarPedidoRequest request) {
        // Apenas criação de pedido
    }
}

@Service
@RequiredArgsConstructor
public class CancelarPedidoUseCase {
    private final PedidoRepositoryPort repository;
    private final NotificacaoPort notificacao;
    
    public void executar(String pedidoId) {
        // Apenas cancelamento de pedido
    }
}

// ❌ ERRADO - Múltiplas responsabilidades
@Service
public class PedidoService {
    public PedidoDTO criar(...) { }
    public void cancelar(...) { }
    public void atualizar(...) { }
    public List<PedidoDTO> buscarTodos(...) { }
    public void enviarNotificacao(...) { }
    public void gerarRelatorio(...) { }
}
```

### O - Open/Closed

```java
// ✅ CORRETO - Extensível via interface
public interface ValidadorPedido {
    void validar(Pedido pedido);
}

@Component
public class ValidadorItensObrigatorios implements ValidadorPedido {
    public void validar(Pedido pedido) {
        if (pedido.getItens().isEmpty()) {
            throw new ValidacaoException("Pedido deve ter itens");
        }
    }
}

@Component
public class ValidadorValorMinimo implements ValidadorPedido {
    public void validar(Pedido pedido) {
        if (pedido.calcularTotal().compareTo(new BigDecimal("10")) < 0) {
            throw new ValidacaoException("Valor mínimo é R$ 10,00");
        }
    }
}

// Adicionar novos validadores sem modificar existentes
@Component
public class ValidadorHorarioFuncionamento implements ValidadorPedido {
    public void validar(Pedido pedido) { ... }
}
```

### L - Liskov Substitution

```java
// ✅ CORRETO - Subtipos são substituíveis
public abstract class Pagamento {
    protected BigDecimal valor;
    protected StatusPagamento status;
    
    public abstract void processar();
    public abstract void estornar();
    
    public BigDecimal getValor() { return valor; }
    public StatusPagamento getStatus() { return status; }
}

public class PagamentoCartao extends Pagamento {
    private final String numeroCartao;
    
    @Override
    public void processar() {
        // Processa cartão
        this.status = StatusPagamento.APROVADO;
    }
    
    @Override
    public void estornar() {
        // Estorna cartão
        this.status = StatusPagamento.ESTORNADO;
    }
}

public class PagamentoPix extends Pagamento {
    private final String chavePix;
    
    @Override
    public void processar() { ... }
    
    @Override
    public void estornar() { ... }
}
```

### I - Interface Segregation

```java
// ✅ CORRETO - Interfaces pequenas e focadas
public interface PedidoRepositoryPort {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(String id);
    List<Pedido> buscarPorCliente(String clienteId);
}

public interface PedidoRelatorioPort {
    List<Pedido> buscarPorPeriodo(LocalDate inicio, LocalDate fim);
    BigDecimal calcularTotalPeriodo(LocalDate inicio, LocalDate fim);
}

public interface PedidoNotificacaoPort {
    void notificarCriacao(Pedido pedido);
    void notificarAtualizacao(Pedido pedido);
}

// ❌ ERRADO - Interface muito grande
public interface PedidoPort {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(String id);
    List<Pedido> buscarTodos();
    List<Pedido> buscarPorCliente(String clienteId);
    List<Pedido> buscarPorPeriodo(LocalDate inicio, LocalDate fim);
    void excluir(String id);
    void notificar(Pedido pedido);
    void enviarEmail(Pedido pedido);
    BigDecimal calcularTotal(String id);
    void gerarRelatorio(LocalDate data);
}
```

### D - Dependency Inversion

```java
// ✅ CORRETO - Depende de abstrações (Port interfaces)
@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {
    // Depende de interfaces (ports), não de implementações
    private final PedidoRepositoryPort repository;  // Interface
    private final ClienteRepositoryPort clienteRepository;  // Interface
    private final NotificacaoPort notificacao;  // Interface
    
    public PedidoDTO executar(CriarPedidoRequest request) {
        Cliente cliente = clienteRepository.buscarPorId(request.clienteId())
            .orElseThrow(() -> new ClienteNaoEncontradoException(request.clienteId()));
            
        Pedido pedido = Pedido.criar(request.itens());
        Pedido salvo = repository.salvar(pedido);
        
        notificacao.notificarNovoPedido(salvo);
        
        return PedidoDTO.from(salvo);
    }
}

// ❌ ERRADO - Depende de implementação concreta
@Service
public class CriarPedidoUseCase {
    private final PedidoJpaRepository repository;  // Implementação concreta
    private final EmailService emailService;  // Implementação concreta
}
```

---

## 5. ESTRUTURA DE PACOTES

### 5.1 Estrutura por Módulo

```
com.snackbar.{modulo}/
├── domain/
│   ├── entities/
│   │   └── Pedido.java
│   ├── valueobjects/
│   │   ├── Preco.java
│   │   └── StatusPedido.java
│   ├── services/
│   │   └── CalculadoraDescontoService.java
│   └── exceptions/
│       ├── PedidoNaoEncontradoException.java
│       └── EstadoInvalidoException.java
│
├── application/
│   ├── usecases/
│   │   ├── CriarPedidoUseCase.java
│   │   ├── BuscarPedidoUseCase.java
│   │   └── CancelarPedidoUseCase.java
│   ├── dtos/
│   │   ├── PedidoDTO.java
│   │   ├── CriarPedidoRequest.java
│   │   └── AtualizarPedidoRequest.java
│   └── ports/
│       ├── in/
│       │   └── PedidoUseCasePort.java
│       └── out/
│           ├── PedidoRepositoryPort.java
│           └── NotificacaoPort.java
│
└── infrastructure/
    ├── persistence/
    │   ├── entities/
    │   │   └── PedidoEntity.java
    │   ├── repositories/
    │   │   ├── PedidoJpaRepository.java
    │   │   └── PedidoRepositoryAdapter.java
    │   └── mappers/
    │       └── PedidoMapper.java
    ├── web/
    │   ├── controllers/
    │   │   └── PedidoController.java
    │   └── handlers/
    │       └── GlobalExceptionHandler.java
    ├── services/
    │   └── NotificacaoAdapter.java
    └── config/
        └── PedidoConfig.java
```

### 5.2 Kernel Compartilhado

```
com.snackbar.kernel/
├── domain/
│   ├── valueobjects/
│   │   └── Dinheiro.java
│   └── exceptions/
│       └── DominioException.java
├── application/
│   ├── dtos/
│   │   └── PaginacaoDTO.java
│   └── ports/
│       └── UseCase.java
└── infrastructure/
    ├── mappers/
    │   ├── JacksonConfig.java
    │   ├── ObjectMapperFactory.java
    │   └── {Feature}Mapper.java
    └── config/
        └── CommonConfig.java
```

---

## 6. CAMADA DOMAIN

### 6.1 Regras da Camada

✅ **PODE TER:**

- Entidades e Aggregates
- Value Objects
- Domain Services
- Exceções de domínio
- Interfaces/Ports (quando necessário)
- Enums de domínio
- Eventos de domínio

❌ **NÃO PODE TER:**

- Anotações Spring (`@Service`, `@Component`, etc.)
- Dependências de frameworks
- Anotações JPA
- Imports de `org.springframework.*`
- Imports de `jakarta.persistence.*`

### 6.2 Exemplos Práticos

```java
// ✅ CORRETO - Entidade de domínio pura
package com.snackbar.pedidos.domain.entities;

import com.snackbar.pedidos.domain.valueobjects.Preco;
import com.snackbar.pedidos.domain.valueobjects.StatusPedido;
import com.snackbar.pedidos.domain.exceptions.EstadoInvalidoException;

public class Pedido {
    private final String id;
    private StatusPedido status;
    private final Preco total;
    
    // Lógica de domínio...
}

// ❌ ERRADO - Domínio poluído com framework
package com.snackbar.pedidos.domain.entities;

import jakarta.persistence.*;  // ❌ JPA no domínio
import org.springframework.stereotype.Component;  // ❌ Spring no domínio

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    private String id;
    
    @Enumerated(EnumType.STRING)
    private StatusPedido status;
}
```

---

## 7. CAMADA APPLICATION

### 7.1 Use Cases

```java
@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {
    private final PedidoRepositoryPort pedidoRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final ValidadorPedidoService validador;
    private final PedidoMapper pedidoMapper;
    
    @Transactional
    public PedidoDTO executar(CriarPedidoRequest request) {
        // 1. Validar request
        validarRequest(request);
        
        // 2. Buscar dependências
        List<Produto> produtos = buscarProdutos(request.itensIds());
        
        // 3. Criar entidade de domínio
        Pedido pedido = criarPedido(request, produtos);
        
        // 4. Validar regras de negócio
        validador.validar(pedido);
        
        // 5. Persistir
        Pedido salvo = pedidoRepository.salvar(pedido);
        
        // 6. Retornar DTO
        return PedidoDTO.from(salvo);
    }
    
    private void validarRequest(CriarPedidoRequest request) {
        if (request.clienteId() == null || request.clienteId().isBlank()) {
            throw new ValidacaoException("Cliente é obrigatório");
        }
        if (request.itensIds() == null || request.itensIds().isEmpty()) {
            throw new ValidacaoException("Pedido deve ter itens");
        }
    }
    
    private List<Produto> buscarProdutos(List<String> ids) {
        return ids.stream()
            .map(id -> produtoRepository.buscarPorId(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id)))
            .toList();
    }
    
    private Pedido criarPedido(CriarPedidoRequest request, List<Produto> produtos) {
        List<ItemPedido> itens = produtos.stream()
            .map(p -> ItemPedido.criar(p, 1))
            .toList();
        return Pedido.criar(request.clienteId(), itens);
    }
}
```

### 7.2 DTOs

```java
// ✅ CORRETO - DTO como record com factory method
public record PedidoDTO(
    String id,
    String clienteId,
    List<ItemPedidoDTO> itens,
    String status,
    BigDecimal total,
    Instant criadoEm
) {
    public static PedidoDTO from(Pedido pedido) {
        return new PedidoDTO(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getItens().stream()
                .map(ItemPedidoDTO::from)
                .toList(),
            pedido.getStatus().name(),
            pedido.calcularTotal(),
            pedido.getCriadoEm()
        );
    }
}

// ✅ CORRETO - Request DTO com validações
public record CriarPedidoRequest(
    @NotBlank(message = "Cliente é obrigatório")
    String clienteId,
    
    @NotEmpty(message = "Pedido deve ter itens")
    List<@Valid ItemPedidoRequest> itens,
    
    String observacao
) { }
```

### 7.3 Ports (Interfaces)

```java
// Port de saída (o que o Use Case precisa)
package com.snackbar.pedidos.application.ports.out;

public interface PedidoRepositoryPort {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(String id);
    List<Pedido> buscarPorCliente(String clienteId);
    List<Pedido> buscarPorStatus(StatusPedido status);
    void excluir(String id);
}

// Port de entrada (o que expõe para fora)
package com.snackbar.pedidos.application.ports.in;

public interface CriarPedidoUseCasePort {
    PedidoDTO executar(CriarPedidoRequest request);
}
```

---

## 8. CAMADA INFRASTRUCTURE

### 8.1 Entities JPA

```java
@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Para JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Para Builder
@Builder
public class PedidoEntity {
    
    @Id
    private String id;
    
    @Column(name = "cliente_id", nullable = false)
    private String clienteId;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedidoEntity> itens = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
    
    @PrePersist
    void prePersist() {
        this.criadoEm = Instant.now();
    }
    
    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = Instant.now();
    }
}
```

### 8.2 Repository Adapter

```java
@Component
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {
    
    private final PedidoJpaRepository jpaRepository;
    private final PedidoMapper mapper;
    
    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoEntity entity = mapper.toEntity(pedido);
        PedidoEntity salvo = jpaRepository.save(entity);
        return mapper.toDomain(salvo);
    }
    
    @Override
    public Optional<Pedido> buscarPorId(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Pedido> buscarPorCliente(String clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Pedido> buscarPorStatus(StatusPedido status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public void excluir(String id) {
        jpaRepository.deleteById(id);
    }
}
```

### 8.3 Controllers

```java
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    
    private final CriarPedidoUseCase criarPedidoUseCase;
    private final BuscarPedidoUseCase buscarPedidoUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    
    @PostMapping
    public ResponseEntity<PedidoDTO> criar(@Valid @RequestBody CriarPedidoRequest request) {
        PedidoDTO pedido = criarPedidoUseCase.executar(request);
        URI location = URI.create("/api/pedidos/" + pedido.id());
        return ResponseEntity.created(location).body(pedido);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable String id) {
        return buscarPedidoUseCase.executar(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
    
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listar(
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String status) {
        var filtro = new FiltroPedido(clienteId, status);
        return ResponseEntity.ok(listarPedidosUseCase.executar(filtro));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        cancelarPedidoUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 9. LOMBOK E JAVA MODERNO (21 / 25 LTS)

### 9.1 Lombok Obrigatório

```java
// ✅ CORRETO - Sempre usar @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class MeuUseCase {
    private final RepositoryPort repository;  // final = injetado
    private final OutroService servico;       // final = injetado
    private final PedidoMapper pedidoMapper;  // mapper injetado via DI
}

// ✅ CORRETO - Entities JPA com Lombok
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProdutoEntity { }

// ❌ PROIBIDO - Construtor manual
@Service
public class MeuUseCase {
    private final RepositoryPort repository;
    
    public MeuUseCase(RepositoryPort repository) {  // ❌ NUNCA
        this.repository = repository;
    }
}
```

### 9.2 Records (Java 16+)

```java
// ✅ CORRETO - Records para DTOs imutáveis
public record ProdutoDTO(
    String id,
    String nome,
    BigDecimal preco,
    String categoria
) {
    // Compact constructor para validação
    public ProdutoDTO {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Objects.requireNonNull(nome, "Nome não pode ser nulo");
    }
    
    // Factory method
    public static ProdutoDTO from(Produto produto) {
        return new ProdutoDTO(
            produto.getId(),
            produto.getNome(),
            produto.getPreco().valor(),
            produto.getCategoria().nome()
        );
    }
}
```

### 9.3 Features Java 17+

```java
// Pattern Matching com instanceof (Java 16+)
if (objeto instanceof Produto produto) {
    System.out.println(produto.getNome());
}

// Switch Expression (Java 14+)
String descricao = switch (status) {
    case PENDENTE -> "Aguardando";
    case EM_PREPARO -> "Preparando";
    case PRONTO -> "Pronto para retirada";
    case ENTREGUE -> "Finalizado";
};

// Text Blocks (Java 15+)
String query = """
    SELECT p.id, p.nome, p.preco
    FROM produtos p
    WHERE p.categoria = :categoria
    ORDER BY p.nome
    """;

// Streams com toList() (Java 16+)
List<String> nomes = produtos.stream()
    .map(Produto::getNome)
    .toList();  // ✅ Java 16+ (não Collectors.toList())

// Sealed Classes/Interfaces (Java 17+)
public sealed interface Pagamento permits PagamentoCartao, PagamentoPix, PagamentoBoleto {}
public final class PagamentoCartao implements Pagamento { /* ... */ }
public final class PagamentoPix implements Pagamento { /* ... */ }
public non-sealed class PagamentoBoleto implements Pagamento { /* ... */ }
```

### 9.4 Features Java 21+ (LTS)

```java
// Record Patterns (Java 21)
if (item instanceof ItemPedido(var produto, var quantidade)) {
    System.out.println(produto.getNome() + " x" + quantidade);
}

// Pattern Matching for Switch (Java 21)
String descricao = switch (pagamento) {
    case PagamentoCartao c -> "Cartão: " + c.getBandeira();
    case PagamentoPix p -> "PIX: " + p.getChave();
    case PagamentoBoleto b -> "Boleto: " + b.getCodigo();
};

// Guarded Patterns
String nivel = switch (pedido) {
    case Pedido p when p.calcularTotal().compareTo(new BigDecimal("1000")) > 0 -> "VIP";
    case Pedido p when p.calcularTotal().compareTo(new BigDecimal("100")) > 0 -> "Premium";
    default -> "Standard";
};

// Sequenced Collections (Java 21)
SequencedCollection<Pedido> pedidos = new LinkedHashSet<>();
Pedido primeiro = pedidos.getFirst();
Pedido ultimo = pedidos.getLast();
SequencedCollection<Pedido> invertidos = pedidos.reversed();

// Virtual Threads (Java 21) — para I/O-bound tasks
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = ids.stream()
        .map(id -> executor.submit(() -> buscarDadosExternos(id)))
        .toList();
}

// ⚠️ Virtual Threads: usar com cautela em Spring Boot
// Habilitar em application.yml:
// spring.threads.virtual.enabled: true
// ✅ Ideal para: chamadas HTTP externas, I/O de arquivo, consultas DB
// ❌ Evitar para: tarefas CPU-bound, sincronização com locks tradicionais
```

### 9.5 Features Java 25 (LTS)

```java
// Structured Concurrency (Java 25 — estável)
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<Pedido> pedidoTask = scope.fork(() -> buscarPedido(id));
    Subtask<Cliente> clienteTask = scope.fork(() -> buscarCliente(clienteId));
    
    scope.join();           // Espera todas
    scope.throwIfFailed();  // Propaga exceções
    
    return montarResposta(pedidoTask.get(), clienteTask.get());
}

// Scoped Values (Java 25 — estável, substitui ThreadLocal)
private static final ScopedValue<UserContext> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.runWhere(CURRENT_USER, userContext, () -> {
    // userContext disponível via CURRENT_USER.get()
    processarRequisicao();
});

// String Templates (Java 25 — preview removido, use formatted/MessageFormat)
String msg = "Pedido %s criado para cliente %s".formatted(pedido.getId(), cliente.getNome());
```

---

## 10. MAPPERS E CONVERSÕES

### 10.1 JacksonConfig + DI (sem singleton manual)

Objetivo: **reutilizar mappers** e evitar custos/reconfiguração repetida, **sem** criar singletons manuais que podem vazar memória em cenários de reload.

Regras:

- ✅ `ObjectMapper` deve existir como **bean** configurado (JacksonConfig).
- ✅ Mappers devem ser **stateless** e **injetados** (beans singleton do Spring).
- ✅ Para variações (views/features), expor via **factories** (ex.: `ObjectReaderFactory`).
- ❌ Proibido `getInstance()`/registry global e `new ObjectMapper()` no código de negócio.

```java
// ✅ CORRETO (exemplo de uso)
@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {
    private final PedidoMapper pedidoMapper;
    private final ObjectMapper objectMapper; // configurado via JacksonConfig
}
```

### 10.2 Mapper Específico de Domínio

```java
@Component
public class PedidoMapper {
    
    public PedidoEntity toEntity(Pedido pedido) {
        return PedidoEntity.builder()
            .id(pedido.getId())
            .clienteId(pedido.getClienteId())
            .status(pedido.getStatus())
            .total(pedido.calcularTotal())
            .criadoEm(pedido.getCriadoEm())
            .itens(pedido.getItens().stream()
                .map(this::toItemEntity)
                .toList())
            .build();
    }
    
    public Pedido toDomain(PedidoEntity entity) {
        List<ItemPedido> itens = entity.getItens().stream()
            .map(this::toItemDomain)
            .toList();
            
        return Pedido.reconstituir(
            entity.getId(),
            entity.getClienteId(),
            itens,
            entity.getStatus(),
            entity.getCriadoEm()
        );
    }
    
    private ItemPedidoEntity toItemEntity(ItemPedido item) {
        return ItemPedidoEntity.builder()
            .id(item.getId())
            .produtoId(item.getProdutoId())
            .quantidade(item.getQuantidade())
            .precoUnitario(item.getPrecoUnitario().valor())
            .build();
    }
    
    private ItemPedido toItemDomain(ItemPedidoEntity entity) {
        return ItemPedido.reconstituir(
            entity.getId(),
            entity.getProdutoId(),
            entity.getQuantidade(),
            Preco.of(entity.getPrecoUnitario())
        );
    }
}
```

### 10.3 Verificar Antes de Criar

```java
// ✅ SEMPRE verifique se já existe em bibliotecas comuns:
// - JacksonConfig (ObjectMapper configurado)
// - Mappers MapStruct (componentModel = "spring")
// - Factories (ObjectReaderFactory/ObjectWriterFactory)

// ✅ SEMPRE verifique se já existe mapper similar no módulo

// ❌ NUNCA crie mapper duplicado
```

---

## 11. PERSISTÊNCIA E REPOSITÓRIOS

### 11.0 ACID e Transações (obrigatório quando houver escrita)

- **Atomicidade**: alterações que precisam “andar juntas” devem estar na mesma transação.
- **Consistência**: valide invariantes no domínio e reforce com constraints no banco.
- **Isolamento**: não assuma; documente quando depender de nível de isolamento/locks.
- **Durabilidade**: sem “write-behind” invisível; commits e migrações versionadas.

Regras práticas:

- ✅ Use transações no nível do **use case/application service** quando fizer sentido.
- ✅ Evite transações longas (I/O externo dentro de transação).
- ✅ Se precisar de integração confiável (mensageria/eventos), avaliar padrão **Outbox**.

### 11.1 JPA Repository Interface

```java
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {
    
    List<PedidoEntity> findByClienteId(String clienteId);
    
    List<PedidoEntity> findByStatus(StatusPedido status);
    
    @Query("SELECT p FROM PedidoEntity p WHERE p.criadoEm BETWEEN :inicio AND :fim")
    List<PedidoEntity> findByPeriodo(
        @Param("inicio") Instant inicio,
        @Param("fim") Instant fim
    );
    
    @Query("SELECT SUM(p.total) FROM PedidoEntity p WHERE p.status = :status")
    Optional<BigDecimal> calcularTotalPorStatus(@Param("status") StatusPedido status);
}
```

### 11.2 Queries Complexas

```java
@Repository
@RequiredArgsConstructor
public class PedidoQueryRepository {
    
    private final EntityManager entityManager;
    
    public List<PedidoResumoDTO> buscarResumoPorPeriodo(LocalDate inicio, LocalDate fim) {
        String jpql = """
            SELECT new com.snackbar.pedidos.application.dtos.PedidoResumoDTO(
                p.id,
                p.clienteId,
                p.status,
                p.total,
                p.criadoEm
            )
            FROM PedidoEntity p
            WHERE p.criadoEm BETWEEN :inicio AND :fim
            ORDER BY p.criadoEm DESC
            """;
            
        return entityManager.createQuery(jpql, PedidoResumoDTO.class)
            .setParameter("inicio", inicio.atStartOfDay().toInstant(ZoneOffset.UTC))
            .setParameter("fim", fim.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
            .getResultList();
    }
}
```

---

## 12. VALIDAÇÕES E EXCEÇÕES

### 12.1 Hierarquia de Exceções

```java
// Exceção base do domínio
public abstract class DominioException extends RuntimeException {
    protected DominioException(String mensagem) {
        super(mensagem);
    }
}

// Exceções específicas
public class EntidadeNaoEncontradaException extends DominioException {
    public EntidadeNaoEncontradaException(String entidade, String id) {
        super(String.format("%s não encontrado(a): %s", entidade, id));
    }
}

public class PedidoNaoEncontradoException extends EntidadeNaoEncontradaException {
    public PedidoNaoEncontradoException(String id) {
        super("Pedido", id);
    }
}

public class RegraDeNegocioException extends DominioException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

public class EstadoInvalidoException extends RegraDeNegocioException {
    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
```

### 12.2 Exception Handler Global

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleEntidadeNaoEncontrada(EntidadeNaoEncontradaException ex) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErroResponse(ex.getMessage(), "NOT_FOUND"));
    }
    
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(RegraDeNegocioException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErroResponse(ex.getMessage(), "BUSINESS_RULE_VIOLATION"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> handleValidacao(MethodArgumentNotValidException ex) {
        List<CampoErro> erros = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new CampoErro(e.getField(), e.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErroValidacaoResponse("Dados inválidos", erros));
    }
}

public record ErroResponse(String mensagem, String codigo) { }
public record CampoErro(String campo, String mensagem) { }
public record ErroValidacaoResponse(String mensagem, List<CampoErro> erros) { }
```

### 12.3 Validação em Use Cases

```java
@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {
    
    public PedidoDTO executar(CriarPedidoRequest request) {
        // Validação de entrada
        validarRequest(request);
        
        // Validação de regras de negócio
        validarRegrasNegocio(request);
        
        // ... resto da lógica
    }
    
    private void validarRequest(CriarPedidoRequest request) {
        List<String> erros = new ArrayList<>();
        
        if (request.clienteId() == null || request.clienteId().isBlank()) {
            erros.add("Cliente é obrigatório");
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            erros.add("Pedido deve ter ao menos um item");
        }
        
        if (!erros.isEmpty()) {
            throw new ValidacaoException(String.join("; ", erros));
        }
    }
}
```

---

## 13. MIGRAÇÕES COM LIQUIBASE

### 13.1 Estrutura de Arquivos

```
src/main/resources/db/changelog/
├── db.changelog-master.yml
├── changes/
│   ├── 20240101120000-criar-tabela-produtos.yml
│   ├── 20240102100000-criar-tabela-pedidos.yml
│   └── 20240103090000-adicionar-coluna-observacao.yml
└── data/
    └── 20240101130000-dados-iniciais.yml
```

### 13.2 Changelog Master

```yaml
# db.changelog-master.yml
databaseChangeLog:
  - includeAll:
      path: db/changelog/changes/
      relativeToChangelogFile: false
```

### 13.3 Changeset Exemplo

```yaml
# 20240101120000-criar-tabela-produtos.yml
databaseChangeLog:
  - changeSet:
      id: 20240101120000-criar-tabela-produtos
      author: desenvolvedor
      changes:
        - createTable:
            tableName: produtos
            columns:
              - column:
                  name: id
                  type: VARCHAR(36)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: nome
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: preco
                  type: DECIMAL(10, 2)
                  constraints:
                    nullable: false
              - column:
                  name: categoria_id
                  type: VARCHAR(36)
                  constraints:
                    nullable: false
                    foreignKeyName: fk_produto_categoria
                    references: categorias(id)
              - column:
                  name: criado_em
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
        
        - createIndex:
            tableName: produtos
            indexName: idx_produto_categoria
            columns:
              - column:
                  name: categoria_id
      
      rollback:
        - dropTable:
            tableName: produtos
```

### 13.4 Regras Obrigatórias

- ✅ **SEMPRE** inclua rollback
- ✅ **SEMPRE** use nomenclatura: `{YYYYMMDDHHmmss}-{descricao}.yml`
- ✅ **SEMPRE** crie índices para campos de busca frequente
- ✅ **SEMPRE** use constraints apropriadas (NOT NULL, UNIQUE, FK)
- ❌ **NUNCA** altere changelogs já aplicados
- ❌ **NUNCA** use IDs sequenciais simples (1, 2, 3)

---

## 14. SEGURANÇA

### 14.1 Autenticação com Spring Security + JWT

```java
// SecurityConfig.java — configuração moderna com SecurityFilterChain
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 14.2 Filtro JWT

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

### 14.3 Token Provider

```java
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:3600000}")  // 1 hora padrão
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }
}
```

### 14.4 Regras de Segurança

| Regra | Detalhe |
|-------|---------|
| Senhas | BCrypt com strength ≥ 12 — **NUNCA** armazenar em texto plano |
| JWT Secret | Mínimo 256 bits, armazenado em variável de ambiente |
| CORS | Configurar origens explícitas — **NUNCA** `allowedOrigins("*")` em produção |
| Rate Limiting | Usar Bucket4j ou Resilience4j para proteger endpoints públicos |
| Dados sensíveis | **NUNCA** logar tokens, senhas, dados pessoais |
| HTTPS | Obrigatório em produção — redirecionar HTTP → HTTPS |
| Headers | HSTS, X-Content-Type-Options, X-Frame-Options configurados |
| Input Validation | Validar **todas** as entradas; usar `@Valid` + Bean Validation |
| SQL Injection | **SEMPRE** usar queries parametrizadas / JPA Criteria |
| Mass Assignment | Usar DTOs dedicados — **NUNCA** expor entidades em endpoints |

### 14.5 OAuth2 / OpenID Connect (quando aplicável)

```yaml
# application.yml — Resource Server com JWT
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com/realms/app
          # OU
          jwk-set-uri: https://auth.example.com/realms/app/protocol/openid-connect/certs
```

```java
// Validação de roles de token OAuth2
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .build();
}

private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthoritiesClaimName("roles");
    converter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
    return jwtConverter;
}
```

---

## 15. LOGGING E OBSERVABILIDADE

### 15.1 Logging Estruturado com SLF4J

```java
// ✅ CORRETO — Logger por classe, mensagens estruturadas
@Slf4j  // Lombok — equivale a: private static final Logger log = LoggerFactory.getLogger(...)
@Service
@RequiredArgsConstructor
public class PedidoService {

    public PedidoResponse criar(CriarPedidoCommand command) {
        log.info("Criando pedido para cliente={}", command.clienteId());
        // ...
        log.info("Pedido criado id={} total={}", pedido.getId(), pedido.getTotal());
        return mapper.toResponse(pedido);
    }
}
```

### 15.2 Níveis de Log

| Nível | Quando usar |
|-------|-------------|
| `ERROR` | Falhas que impedem conclusão da operação; exceções inesperadas |
| `WARN` | Situações anômalas mas recuperáveis; degradação de serviço |
| `INFO` | Eventos de negócio significativos; início/fim de operações |
| `DEBUG` | Detalhes para troubleshooting em desenvolvimento |
| `TRACE` | Detalhes internos granulares — **NUNCA** em produção |

### 15.3 Configuração (application.yml)

```yaml
logging:
  level:
    root: INFO
    com.snackbar: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Produção — JSON para ingestão em ferramentas (ELK, Grafana Loki, etc.)
# Usar logback-spring.xml com encoder JSON:
# <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
```

### 15.4 MDC (Mapped Diagnostic Context)

```java
// Filtro para correlação de requests — essencial para rastreamento
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
            .orElse(UUID.randomUUID().toString());

        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### 15.5 Health Checks e Actuator

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    db:
      enabled: true
```

```java
// Custom Health Indicator
@Component
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final ExternalApiClient apiClient;

    @Override
    public Health health() {
        try {
            apiClient.ping();
            return Health.up().withDetail("externalApi", "reachable").build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

### 15.6 Regras de Observabilidade

- ✅ **SEMPRE** use SLF4J — **NUNCA** `System.out.println()`
- ✅ **SEMPRE** inclua contexto nos logs: IDs, valores de negócio relevantes
- ✅ **SEMPRE** use MDC para correlação de requests
- ✅ **SEMPRE** habilite `/actuator/health` para probes de Kubernetes/Docker
- ✅ **SEMPRE** exponha métricas via Micrometer (`/actuator/prometheus`)
- ❌ **NUNCA** logue dados sensíveis (senhas, tokens, CPF, cartão)
- ❌ **NUNCA** use `e.printStackTrace()` — use `log.error("msg", e)`
- ❌ **NUNCA** logue em loops com alta frequência em produção

---

## 16. API VERSIONING E BOAS PRÁTICAS REST

### 16.1 Versionamento

```java
// ✅ Versão via path (preferido por simplicidade)
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController { }

// ✅ Alternativa: via header (quando retrocompatibilidade é crítica)
@GetMapping(headers = "X-API-Version=2")
public ResponseEntity<PedidoResponseV2> listarV2() { }
```

### 16.2 Padrão de Respostas

```java
// Resposta paginada padronizada
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}

// Resposta de erro padronizada
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
```

### 16.3 Regras REST

| Regra | Detalhe |
|-------|---------|
| Nomenclatura | Recursos no plural, kebab-case: `/api/v1/pedidos`, `/api/v1/itens-pedido` |
| Verbos HTTP | GET (listar/buscar), POST (criar), PUT (atualizar total), PATCH (atualizar parcial), DELETE |
| Status Codes | 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity, 500 Internal Server Error |
| Paginação | **SEMPRE** paginar listas: `?page=0&size=20&sort=createdAt,desc` |
| Filtros | Via query params: `?status=ATIVO&categoria=BEBIDA` |
| HATEOAS | Opcional — usar quando a API é consumida por terceiros |
| Idempotência | PUT e DELETE devem ser idempotentes; POST com idempotency-key quando necessário |

---

## 17. TESTES

### 14.1 Estrutura de Testes

```
src/test/java/com/snackbar/pedidos/
├── domain/
│   ├── entities/
│   │   └── PedidoTest.java
│   └── valueobjects/
│       └── PrecoTest.java
├── application/
│   └── usecases/
│       ├── CriarPedidoUseCaseTest.java
│       └── CancelarPedidoUseCaseTest.java
└── infrastructure/
    ├── persistence/
    │   └── PedidoRepositoryAdapterTest.java
    └── web/
        └── PedidoControllerTest.java
```

### 14.2 Testes de Domínio

```java
class PedidoTest {
    
    @Test
    void deveCriarPedidoComStatusPendente() {
        // Arrange
        List<ItemPedido> itens = List.of(
            ItemPedido.criar(criarProduto(), 2)
        );
        
        // Act
        Pedido pedido = Pedido.criar("cliente-123", itens);
        
        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(pedido.getItens()).hasSize(1);
    }
    
    @Test
    void deveLancarExcecaoAoCriarPedidoSemItens() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> Pedido.criar("cliente-123", List.of()))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessageContaining("deve ter ao menos um item");
    }
    
    @Test
    void deveConfirmarPedidoPendente() {
        // Arrange
        Pedido pedido = criarPedidoPendente();
        
        // Act
        pedido.confirmar();
        
        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }
    
    @Test
    void naoDeveConfirmarPedidoJaConfirmado() {
        // Arrange
        Pedido pedido = criarPedidoConfirmado();
        
        // Act & Assert
        assertThatThrownBy(pedido::confirmar)
            .isInstanceOf(EstadoInvalidoException.class);
    }
}
```

### 14.3 Testes de Use Case

```java
@ExtendWith(MockitoExtension.class)
class CriarPedidoUseCaseTest {
    
    @Mock
    private PedidoRepositoryPort pedidoRepository;
    
    @Mock
    private ProdutoRepositoryPort produtoRepository;
    
    @InjectMocks
    private CriarPedidoUseCase useCase;
    
    @Test
    void deveCriarPedidoComSucesso() {
        // Arrange
        CriarPedidoRequest request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-1", 2))
        );
        
        Produto produto = criarProdutoMock();
        when(produtoRepository.buscarPorId("produto-1"))
            .thenReturn(Optional.of(produto));
        when(pedidoRepository.salvar(any()))
            .thenAnswer(inv -> inv.getArgument(0));
        
        // Act
        PedidoDTO resultado = useCase.executar(request);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.clienteId()).isEqualTo("cliente-123");
        verify(pedidoRepository).salvar(any(Pedido.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        CriarPedidoRequest request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-inexistente", 1))
        );
        
        when(produtoRepository.buscarPorId("produto-inexistente"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.executar(request))
            .isInstanceOf(ProdutoNaoEncontradoException.class);
    }
}
```

### 14.4 Testes de Integração

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PedidoJpaRepository pedidoRepository;
    
    @Test
    void deveCriarPedidoViaApi() throws Exception {
        // Arrange
        CriarPedidoRequest request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-1", 2))
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
    
    @Test
    void deveRetornar404QuandoPedidoNaoExiste() throws Exception {
        mockMvc.perform(get("/api/pedidos/id-inexistente"))
            .andExpect(status().isNotFound());
    }
}
```

---

## 18. ANTI-PATTERNS

### ❌ Proibições Absolutas

1. **Entidades anêmicas** (apenas getters/setters, sem comportamento)
2. **Domínio com dependências de framework** (JPA, Spring no domain)
3. **Construtor manual quando Lombok resolve** (`@RequiredArgsConstructor`)
4. **Singleton manual/registry global** (ex.: `getInstance()`, cache `static`, `new ObjectMapper()` espalhado)
5. **God Classes** com múltiplas responsabilidades
6. **Retornar `null`** quando pode usar `Optional`
7. **Catch vazio** ou genérico demais
8. **Queries SQL** diretamente no Controller
9. **Lógica de negócio** no Controller
10. **Camadas invertidas** (Domain dependendo de Infrastructure)
11. **Use Cases fazendo muitas coisas** (dividir em casos menores)
12. **DTOs sem validação** de entrada

### ⚠️ Code Smells a Evitar

```java
// ❌ Service gigante fazendo tudo
@Service
public class PedidoService {
    // 500+ linhas
    // CRUD + validação + cálculos + notificação + relatórios
}

// ❌ Entidade anêmica
public class Pedido {
    private String id;
    private StatusPedido status;
    
    // Apenas getters e setters
    public void setStatus(StatusPedido status) { this.status = status; }
}

// ❌ Regra de negócio no Controller
@PostMapping
public ResponseEntity<PedidoDTO> criar(@RequestBody CriarPedidoRequest request) {
    if (request.getValor() < 10) {  // ❌ Regra aqui
        throw new RuntimeException("Valor mínimo...");
    }
    // ...
}

// ❌ Domain com JPA
package com.snackbar.pedidos.domain.entities;
@Entity  // ❌ JPA no domínio
public class Pedido { }
```

---

## 19. CHECKLIST DE QUALIDADE

### Antes de Criar uma Classe

- [ ] Verificar se não existe classe similar
- [ ] Definir em qual camada pertence (Domain/Application/Infrastructure)
- [ ] Planejar responsabilidade única
- [ ] Definir se usará Lombok

### Antes de Commitar

- [ ] Clean Architecture respeitada (dependências corretas)
- [ ] Domain sem dependências de framework
- [ ] Usando `@RequiredArgsConstructor` (não construtor manual)
- [ ] Usando mappers/factories via DI (JacksonConfig / MapStruct), sem singleton manual
- [ ] Use Cases pequenos e focados
- [ ] Entidades ricas (com comportamento)
- [ ] Value Objects imutáveis
- [ ] Exceções específicas e tratadas
- [ ] Arquivos com menos de 300 linhas
- [ ] Métodos com menos de 20 linhas
- [ ] Testes para lógica de negócio
- [ ] Migrações Liquibase com rollback

### Revisão de Arquitetura

- [ ] Interfaces (Ports) definidas na Application
- [ ] Implementações (Adapters) na Infrastructure
- [ ] DTOs na Application, não no Domain
- [ ] Validações de entrada nos Use Cases
- [ ] Regras de negócio nas Entidades/Domain Services

---

## 📚 RECURSOS DO PROJETO

### Kernel Compartilhado

```java
// Mappers/Jackson (reuso sem singleton manual)
JacksonConfig
ObjectMapperFactory / ObjectReaderFactory / ObjectWriterFactory
{Feature}Mapper (MapStruct componentModel = "spring", quando aplicável)

// Value Objects comuns
Dinheiro.of(valor)

// Exceções base
DominioException
EntidadeNaoEncontradaException
```

### Estrutura de Módulos

```
├── kernel-compartilhado/    # Classes compartilhadas
├── autenticacao/            # Módulo de autenticação
├── gestao-cardapio/         # Gestão de cardápio
├── gestao-clientes/         # Gestão de clientes
├── gestao-pedidos/          # Gestão de pedidos
├── impressao-cupom-fiscal/  # Impressão fiscal
└── sistema-orquestrador/    # Orquestração de serviços
```

---

**🚨 LEMBRE-SE: Código Java limpo é tipado, imutável quando possível, e segue Clean Architecture rigorosamente!**
