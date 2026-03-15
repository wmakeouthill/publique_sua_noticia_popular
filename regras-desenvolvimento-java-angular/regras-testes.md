# Regras e Boas Práticas de Testes - Stack Java + Angular

> **Documento de Referência** — Práticas equivalentes aos documentos de testes Python e .NET

---

## 📋 ÍNDICE

1. [Frameworks e Tecnologias](#1-frameworks-e-tecnologias)
2. [Estrutura e Organização](#2-estrutura-e-organização)
3. [Padrões de Nomenclatura](#3-padrões-de-nomenclatura)
4. [Testes Unitários - Domain](#4-testes-unitários---domain)
5. [Testes Unitários - Use Cases / Services](#5-testes-unitários---use-cases--services)
6. [Testes de Integração - API](#6-testes-de-integração---api)
7. [Mocking com Mockito](#7-mocking-com-mockito)
8. [Fixtures e Builders](#8-fixtures-e-builders)
9. [Testes Parametrizados](#9-testes-parametrizados)
10. [Testes Frontend (Angular)](#10-testes-frontend-angular)
11. [Cobertura de Código](#11-cobertura-de-código)
12. [Anti-Patterns de Testes](#12-anti-patterns-de-testes)

---

## 1. FRAMEWORKS E TECNOLOGIAS

### 1.1 Backend Java

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **JUnit 5** | 5.10+ | Framework principal de testes |
| **AssertJ** | 3.25+ | Assertions fluentes e legíveis |
| **Mockito** | 5.x | Mocking (via `@ExtendWith(MockitoExtension.class)`) |
| **Testcontainers** | 1.19+ | Containers para testes de integração (PostgreSQL, etc.) |
| **Spring Boot Test** | 3.x | `@SpringBootTest`, `MockMvc`, `WebTestClient` |
| **ArchUnit** | 1.2+ | Validação de regras arquiteturais |
| **Instancio** | 4.x | Geração automática de objetos de teste |
| **Faker** | 2.x (datafaker) | Dados fake realistas |
| **WireMock** | 3.x | Mock de APIs HTTP externas |

### 1.2 Frontend Angular

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Jasmine** | 5.x | Framework de testes unitários |
| **Karma** | 6.x | Test runner (ou migrar para **Jest/Vitest** via `@analogjs`) |
| **Angular Testing Library** | 17+ | Testes baseados em comportamento |
| **Cypress** / **Playwright** | 13+ / 1.48+ | Testes E2E |
| **ng-mocks** | 14+ | Mock de componentes/directives/pipes Angular |
| **MSW** | 2.x | Mock de APIs HTTP no frontend |

### 1.3 Correspondência entre Stacks

| Conceito | Java | Python | .NET |
|----------|------|--------|------|
| Framework de testes | JUnit 5 | pytest | xUnit |
| Mocking | Mockito | pytest-mock / AsyncMock | NSubstitute |
| Assertions fluentes | AssertJ | assert + pytest | FluentAssertions |
| Dados fake | DataFaker / Instancio | factory-boy / Faker | Bogus |
| Containers de teste | Testcontainers | Testcontainers-py | Testcontainers.NET / InMemory |
| Validação arquitetural | ArchUnit | pytest-archon | NetArchTest |
| Testes de API | MockMvc | httpx + AsyncClient | WebApplicationFactory |

---

## 2. ESTRUTURA E ORGANIZAÇÃO

### 2.1 Backend Java

```
src/test/java/com/app/pedidos/
├── domain/
│   ├── entities/
│   │   ├── PedidoTest.java
│   │   └── ProdutoTest.java
│   ├── valueobjects/
│   │   ├── PrecoTest.java
│   │   └── CPFTest.java
│   └── services/
│       └── CalculadoraDescontoServiceTest.java
├── application/
│   ├── usecases/
│   │   ├── CriarPedidoUseCaseTest.java
│   │   └── CancelarPedidoUseCaseTest.java
│   └── validators/
│       └── CriarPedidoRequestValidatorTest.java
├── infrastructure/
│   ├── persistence/
│   │   └── PedidoRepositoryAdapterTest.java
│   └── web/
│       ├── PedidoControllerTest.java
│       └── PedidoControllerIntegrationTest.java
├── architecture/
│   └── ArchitectureTest.java
└── fixtures/
    ├── PedidoFixture.java
    └── ProdutoFixture.java
```

### 2.2 Frontend Angular

```
src/app/
├── features/pedidos/
│   ├── components/
│   │   ├── pedido-card.component.ts
│   │   └── pedido-card.component.spec.ts    # Co-located
│   ├── pages/
│   │   ├── pedido-list.page.ts
│   │   └── pedido-list.page.spec.ts
│   └── services/
│       ├── pedido.service.ts
│       └── pedido.service.spec.ts
├── shared/
│   └── utils/
│       ├── formatters.ts
│       └── formatters.spec.ts
└── test/
    ├── setup.ts
    └── mocks/
        └── handlers.ts                       # MSW handlers
```

### 2.3 Regras de Organização

| Regra | Detalhe |
|-------|---------|
| Espelho | Estrutura de testes espelha a estrutura do `src/main` |
| Nomenclatura | `ClasseTestada` + `Test.java` (backend), `*.spec.ts` (frontend) |
| Co-location | Testes Angular ficam junto do arquivo testado |
| Fixtures | Centralizadas em package `fixtures/` — reutilizáveis |
| 1 classe de teste por classe de produção | Exceção: classes muito complexas podem ter múltiplas |

---

## 3. PADRÕES DE NOMENCLATURA

### 3.1 Nomenclatura de Métodos

```java
// Padrão: deve + Ação + Quando + Cenário
@Test
void deveCriarPedidoComStatusPendente() {}

@Test
void deveLancarExcecaoQuandoItensVazio() {}

@Test
void deveCalcularTotalCorretamenteComMultiplosItens() {}

// Padrão alternativo: metodo_cenario_resultado
@Test
void criar_comItensValidos_deveRetornarPedidoPendente() {}

@Test
void confirmar_quandoJaConfirmado_deveLancarEstadoInvalido() {}
```

### 3.2 Nomenclatura de Classes

```java
// Classe de teste = ClasseTestada + Test
class PedidoTest {}
class CriarPedidoUseCaseTest {}
class PedidoControllerIntegrationTest {}  // Sufixo IntegrationTest para testes pesados
```

### 3.3 Organização Interna

```java
class PedidoTest {

    // ✅ Agrupar com @Nested para cenários
    @Nested
    class Criar {
        @Test
        void deveCriarComStatusPendente() { /* ... */ }

        @Test
        void deveLancarExcecaoSemItens() { /* ... */ }
    }

    @Nested
    class Confirmar {
        @Test
        void deveAlterarParaConfirmado() { /* ... */ }

        @Test
        void deveLancarExcecaoQuandoJaConfirmado() { /* ... */ }
    }

    @Nested
    class CalcularTotal {
        @Test
        void deveSomarItensCorretamente() { /* ... */ }
    }
}
```

---

## 4. TESTES UNITÁRIOS - DOMAIN

### 4.1 Testando Entidades

```java
class PedidoTest {

    @Test
    void deveCriarPedidoComStatusPendente() {
        // Arrange
        var itens = List.of(ItemPedido.criar(criarProduto(), 2));

        // Act
        var pedido = Pedido.criar("cliente-123", itens);

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(pedido.getItens()).hasSize(1);
        assertThat(pedido.getCriadoEm()).isCloseTo(
            LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void deveLancarExcecaoAoCriarSemItens() {
        assertThatThrownBy(() -> Pedido.criar("cliente-123", List.of()))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessageContaining("ao menos um item");
    }

    @Test
    void deveConfirmarPedidoPendente() {
        var pedido = PedidoFixture.pendente();

        pedido.confirmar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void naoDeveConfirmarPedidoJaConfirmado() {
        var pedido = PedidoFixture.confirmado();

        assertThatThrownBy(pedido::confirmar)
            .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void deveCalcularTotalCorretamente() {
        var itens = List.of(
            ItemPedido.criar(criarProduto(new BigDecimal("10.00")), 2),  // 20
            ItemPedido.criar(criarProduto(new BigDecimal("15.00")), 1)   // 15
        );
        var pedido = Pedido.criar("cliente-123", itens);

        var total = pedido.calcularTotal();

        assertThat(total).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    // Helpers privados
    private static Produto criarProduto() {
        return criarProduto(new BigDecimal("10.00"));
    }

    private static Produto criarProduto(BigDecimal preco) {
        return Produto.criar("Produto Teste", Preco.of(preco));
    }
}
```

### 4.2 Testando Value Objects

```java
class PrecoTest {

    @Test
    void deveCriarPrecoComValorPositivo() {
        var preco = Preco.of(new BigDecimal("100.50"));

        assertThat(preco.getValor()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0.01", "-100"})
    void deveLancarExcecaoComValorNegativo(String valorNegativo) {
        assertThatThrownBy(() -> Preco.of(new BigDecimal(valorNegativo)))
            .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void deveSomarDoisPrecos() {
        var preco1 = Preco.of(new BigDecimal("10.00"));
        var preco2 = Preco.of(new BigDecimal("20.00"));

        var resultado = preco1.somar(preco2);

        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void zeroDeveRetornarPrecoComValorZero() {
        assertThat(Preco.ZERO.getValor()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

class CPFTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678909", "123.456.789-09"})
    void deveCriarCPFValido(String cpfValido) {
        var cpf = CPF.of(cpfValido);

        assertThat(cpf).isNotNull();
        assertThat(cpf.getValor()).hasSize(11);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "11111111111", "abcdefghijk"})
    void deveLancarExcecaoComCPFInvalido(String cpfInvalido) {
        assertThatThrownBy(() -> CPF.of(cpfInvalido))
            .isInstanceOf(CPFInvalidoException.class);
    }

    @Test
    void formatadoDeveRetornarComMascara() {
        var cpf = CPF.of("12345678909");

        assertThat(cpf.formatado()).isEqualTo("123.456.789-09");
    }
}
```

---

## 5. TESTES UNITÁRIOS - USE CASES / SERVICES

### 5.1 Testando com Mocks

```java
@ExtendWith(MockitoExtension.class)
class CriarPedidoUseCaseTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    @InjectMocks
    private CriarPedidoUseCase sut;  // system under test

    @Test
    void deveSalvarPedidoComSucesso() {
        // Arrange
        var produtoId = "produto-1";
        var request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest(produtoId, 2))
        );
        var produto = ProdutoFixture.valido();

        when(produtoRepository.buscarPorId(produtoId))
            .thenReturn(Optional.of(produto));
        when(pedidoRepository.salvar(any(Pedido.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Act
        var resultado = sut.executar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.clienteId()).isEqualTo("cliente-123");
        assertThat(resultado.itens()).hasSize(1);
        assertThat(resultado.status()).isEqualTo("PENDENTE");

        verify(pedidoRepository).salvar(any(Pedido.class));
        verify(produtoRepository).buscarPorId(produtoId);
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        var request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-inexistente", 1))
        );

        when(produtoRepository.buscarPorId("produto-inexistente"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.executar(request))
            .isInstanceOf(ProdutoNaoEncontradoException.class);

        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoValidacaoFalha() {
        // Arrange
        var request = new CriarPedidoRequest("", List.of());

        // Act & Assert
        assertThatThrownBy(() -> sut.executar(request))
            .isInstanceOf(ValidacaoException.class);
    }
}
```

### 5.2 BDDMockito (alternativa expressiva)

```java
import static org.mockito.BDDMockito.*;

@Test
void deveRetornarPedidoPorId() {
    // Given
    var pedido = PedidoFixture.confirmado();
    given(pedidoRepository.buscarPorId("pedido-1"))
        .willReturn(Optional.of(pedido));

    // When
    var resultado = sut.buscarPorId("pedido-1");

    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.status()).isEqualTo("CONFIRMADO");
    then(pedidoRepository).should().buscarPorId("pedido-1");
}
```

---

## 6. TESTES DE INTEGRAÇÃO - API

### 6.1 Com MockMvc (sem subir container)

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
    private PedidoJpaRepository repository;

    @Test
    void deveCriarPedidoViaApi() throws Exception {
        var request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-1", 2))
        );

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("PENDENTE"))
            .andExpect(header().exists("Location"));
    }

    @Test
    void deveRetornar400ComDadosInvalidos() throws Exception {
        var request = new CriarPedidoRequest("", List.of());

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").exists());
    }

    @Test
    void deveRetornar404QuandoPedidoNaoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveListarPedidosPaginados() throws Exception {
        // Arrange — Seed dados
        repository.save(PedidoFixture.entidadeJpa());
        repository.save(PedidoFixture.entidadeJpa());

        // Act & Assert
        mockMvc.perform(get("/api/v1/pedidos")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.totalElements").exists());
    }
}
```

### 6.2 Com Testcontainers (PostgreSQL real)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PedidoEndToEndTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void devePersistirPedidoRealmente() throws Exception {
        var request = new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-1", 2))
        );

        var result = mockMvc.perform(post("/api/v1/pedidos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        var location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();

        // Verificar que GET retorna o pedido
        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").value("cliente-123"));
    }
}
```

### 6.3 Mock de APIs Externas com WireMock

```java
@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8089)
class PagamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveProcessarPagamentoComGatewayExterno() {
        // Arrange — Mock do gateway de pagamento
        stubFor(post(urlEqualTo("/api/pagamentos"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"transacaoId": "txn-123", "status": "APROVADO"}
                    """)));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pedidos/{id}/pagar", pedidoId)
                .contentType(APPLICATION_JSON)
                .content("{\"metodo\": \"PIX\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagamentoStatus").value("APROVADO"));

        // Verificar chamada
        verify(postRequestedFor(urlEqualTo("/api/pagamentos"))
            .withRequestBody(containsString("PIX")));
    }
}
```

---

## 7. MOCKING COM MOCKITO

### 7.1 Setup com @Mock

```java
@ExtendWith(MockitoExtension.class)
class ExemploMockTest {

    @Mock
    private PedidoRepositoryPort repository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private ProcessarPedidoUseCase sut;

    // Testes aqui...
}
```

### 7.2 Configurando Retornos

```java
// Retorno simples
when(repository.buscarPorId("id-1")).thenReturn(Optional.of(pedido));

// Retorno dinâmico
when(repository.salvar(any(Pedido.class)))
    .thenAnswer(inv -> inv.getArgument(0));

// Retorno sequencial
when(repository.buscarPorId(any()))
    .thenReturn(Optional.of(pedido1))
    .thenReturn(Optional.of(pedido2))
    .thenReturn(Optional.empty());

// Lançar exceção
when(repository.buscarPorId("invalido"))
    .thenThrow(new RuntimeException("Erro de conexão"));

// void method com doNothing/doThrow
doNothing().when(notificacaoService).enviar(any());
doThrow(new NotificacaoException("falha"))
    .when(notificacaoService).enviar(any());
```

### 7.3 Verificando Chamadas

```java
// Verificar que foi chamado
verify(repository).salvar(any(Pedido.class));

// Verificar número de chamadas
verify(repository, times(1)).salvar(any());
verify(notificacaoService, never()).enviar(any());

// Verificar ordem
InOrder inOrder = inOrder(repository, notificacaoService);
inOrder.verify(repository).salvar(any());
inOrder.verify(notificacaoService).enviar(any());
```

### 7.4 Capturando Argumentos

```java
@Captor
private ArgumentCaptor<Pedido> pedidoCaptor;

@Test
void deveCapturarPedidoSalvo() {
    // Act
    sut.executar(request);

    // Assert
    verify(repository).salvar(pedidoCaptor.capture());
    var pedidoSalvo = pedidoCaptor.getValue();

    assertThat(pedidoSalvo.getClienteId()).isEqualTo("cliente-123");
    assertThat(pedidoSalvo.getStatus()).isEqualTo(StatusPedido.PENDENTE);
    assertThat(pedidoSalvo.getItens()).hasSize(2);
}
```

### 7.5 Mocking de Static / Time

```java
// Mockar LocalDateTime.now() com Clock (preferido)
// Injetar Clock como dependência
@Service
@RequiredArgsConstructor
public class PedidoService {
    private final Clock clock;  // Injetar em vez de usar LocalDateTime.now()

    public Pedido criar() {
        return new Pedido(LocalDateTime.now(clock));
    }
}

// No teste
@Test
void deveCriarComDataFixa() {
    var fixedInstant = LocalDateTime.of(2025, 1, 1, 10, 0)
        .atZone(ZoneId.systemDefault()).toInstant();
    var fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());

    var service = new PedidoService(fixedClock);
    var pedido = service.criar();

    assertThat(pedido.getCriadoEm())
        .isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
}

// Alternativa: MockedStatic para legado
try (var mockedTime = mockStatic(LocalDateTime.class)) {
    mockedTime.when(LocalDateTime::now).thenReturn(dataFixa);
    // ...
}
```

---

## 8. FIXTURES E BUILDERS

### 8.1 Fixtures com Métodos Estáticos

```java
public final class PedidoFixture {

    private PedidoFixture() {} // utilitário

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    public static Pedido pendente() {
        return Pedido.criar(
            faker.internet().uuid(),
            List.of(ItemPedido.criar(ProdutoFixture.valido(), 1))
        );
    }

    public static Pedido confirmado() {
        var pedido = pendente();
        pedido.confirmar();
        return pedido;
    }

    public static Pedido comTotal(BigDecimal total) {
        var preco = Preco.of(total);
        var produto = Produto.criar("Produto", preco);
        return Pedido.criar("cliente-1", List.of(ItemPedido.criar(produto, 1)));
    }

    public static CriarPedidoRequest requestValido() {
        return new CriarPedidoRequest(
            "cliente-123",
            List.of(new ItemPedidoRequest("produto-1", 2))
        );
    }
}

public final class ProdutoFixture {

    private ProdutoFixture() {}

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    public static Produto valido() {
        return Produto.criar(
            faker.commerce().productName(),
            Preco.of(BigDecimal.valueOf(faker.number().randomDouble(2, 5, 500)))
        );
    }

    public static Produto comPreco(BigDecimal preco) {
        return Produto.criar("Produto Teste", Preco.of(preco));
    }
}
```

### 8.2 Builder Pattern para Testes

```java
public class PedidoTestBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    private String clienteId = faker.internet().uuid();
    private StatusPedido status = StatusPedido.PENDENTE;
    private final List<ItemPedido> itens = new ArrayList<>();

    public static PedidoTestBuilder umPedido() {
        return new PedidoTestBuilder();
    }

    public PedidoTestBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public PedidoTestBuilder comStatus(StatusPedido status) {
        this.status = status;
        return this;
    }

    public PedidoTestBuilder comItem(Produto produto, int quantidade) {
        this.itens.add(ItemPedido.criar(produto, quantidade));
        return this;
    }

    public PedidoTestBuilder comItensAleatorios(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            itens.add(ItemPedido.criar(ProdutoFixture.valido(), faker.number().numberBetween(1, 5)));
        }
        return this;
    }

    public Pedido build() {
        if (itens.isEmpty()) comItensAleatorios(1);

        var pedido = Pedido.criar(clienteId, itens);

        if (status == StatusPedido.CONFIRMADO) pedido.confirmar();
        return pedido;
    }
}

// Uso
@Test
void exemplo() {
    var pedido = PedidoTestBuilder.umPedido()
        .comCliente("cliente-vip")
        .comItensAleatorios(3)
        .build();
}
```

### 8.3 Instancio (geração automática de objetos)

```java
import org.instancio.Instancio;
import static org.instancio.Select.field;

@Test
void deveGerarPedidoAleatoriamente() {
    var pedido = Instancio.of(Pedido.class)
        .set(field(Pedido::getClienteId), "cliente-fixo")
        .set(field(Pedido::getStatus), StatusPedido.PENDENTE)
        .generate(field(Pedido::getCriadoEm), gen -> gen.temporal().localDateTime().past())
        .create();

    assertThat(pedido.getClienteId()).isEqualTo("cliente-fixo");
}
```

---

## 9. TESTES PARAMETRIZADOS

### 9.1 @ValueSource

```java
@ParameterizedTest
@ValueSource(strings = {"", "   ", "  \t  "})
void deveRejeitarNomesVazios(String nomeInvalido) {
    assertThatThrownBy(() -> Nome.of(nomeInvalido))
        .isInstanceOf(ValorInvalidoException.class);
}

@ParameterizedTest
@ValueSource(ints = {0, -1, -100})
void deveRejeitarQuantidadeInvalida(int qtdInvalida) {
    assertThatThrownBy(() -> ItemPedido.criar(ProdutoFixture.valido(), qtdInvalida))
        .isInstanceOf(ValorInvalidoException.class);
}
```

### 9.2 @CsvSource

```java
@ParameterizedTest
@CsvSource({
    "1,  10.00, 10.00",
    "2,  15.00, 30.00",
    "3,  20.00, 60.00",
    "10, 5.50,  55.00"
})
void deveCalcularSubtotal(int quantidade, BigDecimal precoUnitario, BigDecimal esperado) {
    var produto = ProdutoFixture.comPreco(precoUnitario);
    var item = ItemPedido.criar(produto, quantidade);

    assertThat(item.getSubtotal()).isEqualByComparingTo(esperado);
}
```

### 9.3 @MethodSource

```java
@ParameterizedTest
@MethodSource("cenariosDePedidoInvalido")
void deveRejeitarPedidoInvalido(CriarPedidoRequest request, String campoEsperado) {
    assertThatThrownBy(() -> sut.executar(request))
        .isInstanceOf(ValidacaoException.class)
        .satisfies(ex -> {
            var erros = ((ValidacaoException) ex).getErros();
            assertThat(erros).anyMatch(e -> e.campo().equals(campoEsperado));
        });
}

static Stream<Arguments> cenariosDePedidoInvalido() {
    return Stream.of(
        Arguments.of(new CriarPedidoRequest("", List.of(new ItemPedidoRequest("p1", 1))), "clienteId"),
        Arguments.of(new CriarPedidoRequest("c1", List.of()), "itens"),
        Arguments.of(new CriarPedidoRequest(null, null), "clienteId")
    );
}
```

### 9.4 @EnumSource

```java
@ParameterizedTest
@EnumSource(value = StatusPedido.class, names = {"CANCELADO", "ENTREGUE"})
void naoDeveConfirmarPedidoFinalizado(StatusPedido statusFinal) {
    var pedido = PedidoTestBuilder.umPedido()
        .comStatus(statusFinal)
        .build();

    assertThatThrownBy(pedido::confirmar)
        .isInstanceOf(EstadoInvalidoException.class);
}
```

---

## 10. TESTES FRONTEND (ANGULAR)

### 10.1 Setup e Configuração

```typescript
// karma.conf.js ou vitest.config.ts (se usar Analog)
// Com Jasmine/Karma (padrão Angular):
```

### 10.2 Testes de Componentes com TestBed

```typescript
describe('PedidoCardComponent', () => {
  let component: PedidoCardComponent;
  let fixture: ComponentFixture<PedidoCardComponent>;

  const pedidoMock: Pedido = {
    id: '1',
    clienteId: 'c1',
    itens: [],
    status: 'PENDENTE',
    total: 50.0,
    criadoEm: '2025-01-01',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PedidoCardComponent],  // Standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(PedidoCardComponent);
    component = fixture.componentInstance;
  });

  it('deve exibir informações do pedido', () => {
    fixture.componentRef.setInput('pedido', pedidoMock);
    fixture.detectChanges();

    const el = fixture.nativeElement;
    expect(el.textContent).toContain('PENDENTE');
    expect(el.textContent).toContain('R$ 50,00');
  });

  it('deve emitir evento ao clicar em excluir', () => {
    fixture.componentRef.setInput('pedido', pedidoMock);
    fixture.detectChanges();

    const spy = spyOn(component.excluir, 'emit');
    const btn = fixture.nativeElement.querySelector('[data-testid="btn-excluir"]');
    btn.click();

    expect(spy).toHaveBeenCalledWith('1');
  });
});
```

### 10.3 Testes de Services com HttpClient

```typescript
describe('PedidoService', () => {
  let service: PedidoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PedidoService],
    });

    service = TestBed.inject(PedidoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve listar pedidos', () => {
    const mockResponse = { content: [{ id: '1', status: 'PENDENTE' }], totalElements: 1 };

    service.listar(0, 10).subscribe(data => {
      expect(data.content).toHaveSize(1);
      expect(data.content[0].status).toBe('PENDENTE');
    });

    const req = httpMock.expectOne('/api/v1/pedidos?page=0&size=10');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('deve criar pedido', () => {
    const request = { clienteId: 'c1', itens: [{ produtoId: 'p1', quantidade: 2 }] };

    service.criar(request).subscribe(res => {
      expect(res.id).toBeDefined();
    });

    const req = httpMock.expectOne('/api/v1/pedidos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: '123' });
  });
});
```

### 10.4 Testes de Composables (Signals)

```typescript
describe('usePedidosFiltro', () => {
  it('deve filtrar por status', () => {
    TestBed.runInInjectionContext(() => {
      const composable = usePedidosFiltro();

      composable.setFiltro({ status: 'PENDENTE' });

      expect(composable.filtroAtual()).toEqual({ status: 'PENDENTE' });
    });
  });

  it('deve resetar filtros', () => {
    TestBed.runInInjectionContext(() => {
      const composable = usePedidosFiltro();

      composable.setFiltro({ status: 'ATIVO' });
      composable.resetar();

      expect(composable.filtroAtual()).toEqual({});
    });
  });
});
```

### 10.5 Testes com ng-mocks

```typescript
import { MockComponent, MockPipe } from 'ng-mocks';

describe('PedidoListPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PedidoListPageComponent,
        MockComponent(PedidoCardComponent),   // Mock do child component
        MockComponent(LoadingSpinnerComponent),
        MockPipe(CurrencyPipe),
      ],
      providers: [
        { provide: PedidoService, useValue: jasmine.createSpyObj('PedidoService', ['listar']) },
      ],
    }).compileComponents();
  });

  it('deve exibir lista quando dados carregam', async () => {
    // ...
  });
});
```

### 10.6 Regras de Testes Frontend

- ✅ **SEMPRE** testar comportamento, não implementação interna
- ✅ **SEMPRE** usar `fixture.componentRef.setInput()` para Signals inputs
- ✅ **SEMPRE** usar `HttpTestingController` para testar services HTTP
- ✅ **SEMPRE** verificar `httpMock.verify()` no `afterEach`
- ✅ **SEMPRE** usar `ng-mocks` para isolar componentes em testes unitários
- ❌ **NUNCA** testar detalhes internos (signals privados, estado interno)
- ❌ **NUNCA** acessar `component.someSignal()` sem `fixture.detectChanges()`
- ❌ **NUNCA** usar `setTimeout` para esperar assíncronos — usar `fakeAsync` + `tick`

---

## 11. COBERTURA DE CÓDIGO

### 11.1 Maven Surefire + JaCoCo

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 11.2 Angular Coverage (Karma/Istanbul)

```json
// angular.json — configuração de cobertura
{
  "test": {
    "options": {
      "codeCoverage": true,
      "codeCoverageExclude": [
        "src/test/**",
        "**/*.spec.ts",
        "src/environments/**"
      ]
    }
  }
}
```

```bash
# Rodar com cobertura
ng test --code-coverage

# Com threshold mínimo
ng test --code-coverage --code-coverage-exclude="src/test/**"
```

### 11.3 Comandos Backend

```bash
# Testes unitários + cobertura
mvn test jacoco:report

# Apenas testes de integração
mvn verify -Pfailsafe

# Relatório HTML
# target/site/jacoco/index.html
```

### 11.4 Metas de Cobertura

| Camada | Meta Mínima | Meta Ideal |
|--------|-------------|------------|
| Domain (Entities + VOs) | 90% | 95%+ |
| Application (Use Cases) | 80% | 90%+ |
| Infrastructure (Adapters) | 70% | 80%+ |
| Web (Controllers) | 70% | 80%+ |
| Frontend (Components) | 70% | 85%+ |
| Frontend (Services/Hooks) | 75% | 85%+ |

### 11.5 Correspondência de Metas

| Camada | Java | Python | .NET |
|--------|------|--------|------|
| Domain | 90% | 80%+ | 90% |
| Application | 80% | 75%+ | 80% |
| Infrastructure | 70% | 70%+ | 70% |
| API/Controllers | 70% | 70%+ | 70% |

---

## 12. ANTI-PATTERNS DE TESTES

### ❌ Testes Sem Assertions

```java
// ❌ ERRADO — O que foi verificado?
@Test
void deveProcessar() {
    sut.processarPedido(UUID.randomUUID());
}

// ✅ CORRETO
@Test
void deveAtualizarStatusAoProcessar() {
    var id = UUID.randomUUID();
    sut.processarPedido(id);

    verify(repository).salvar(argThat(p ->
        p.getStatus() == StatusPedido.PROCESSADO));
}
```

### ❌ Mocking Excessivo

```java
// ❌ ERRADO — Mockar value objects simples
var mockPreco = mock(Preco.class);
when(mockPreco.getValor()).thenReturn(BigDecimal.TEN);

// ✅ CORRETO — Usar instância real
var preco = Preco.of(BigDecimal.TEN);
```

### ❌ Testes Acoplados à Implementação

```java
// ❌ ERRADO — Testa detalhes internos
@Test
void deveChamarValidarItensPrivado() {
    // Tenta testar método privado
}

// ✅ CORRETO — Testar comportamento público
@Test
void deveLancarExcecaoSemItens() {
    assertThatThrownBy(() -> Pedido.criar("c1", List.of()))
        .isInstanceOf(RegraDeNegocioException.class);
}
```

### ❌ Testes Não Determinísticos (Flaky)

```java
// ❌ ERRADO — Depende de timing
@Test
void deveProcessarRapido() {
    var start = System.currentTimeMillis();
    sut.processar();
    assertThat(System.currentTimeMillis() - start).isLessThan(100);  // Flaky!
}

// ❌ ERRADO — Depende de LocalDateTime.now()
@Test
void deveCriarHoje() {
    var pedido = Pedido.criar(...);
    assertThat(pedido.getCriadoEm().toLocalDate())
        .isEqualTo(LocalDate.now());  // Falha à meia-noite!
}

// ✅ CORRETO — Faixa de tempo
@Test
void deveSetarDataCriacao() {
    var antes = LocalDateTime.now();
    var pedido = Pedido.criar("c1", itens);
    var depois = LocalDateTime.now();

    assertThat(pedido.getCriadoEm())
        .isAfterOrEqualTo(antes)
        .isBeforeOrEqualTo(depois);
}
```

### ❌ Testes que Dependem de Ordem

```java
// ❌ ERRADO — Estado compartilhado entre testes
static int contador = 0;

@Test void teste1() { contador++; }
@Test void teste2() { assertThat(contador).isEqualTo(1); }  // Pode falhar!

// ✅ CORRETO — Cada teste é independente com @BeforeEach
```

### ❌ Testes Acessando Recursos Externos

```java
// ❌ ERRADO — Chama API real
@Test
void deveBuscarDados() {
    var response = new RestTemplate().getForEntity("https://api-real.com/dados", String.class);
    // ...
}

// ✅ CORRETO — Usar WireMock ou MockMvc
```

---

## 13. VALIDAÇÃO ARQUITETURAL (ARCHUNIT)

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.app")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainNaoDependeDeInfrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domainNaoDependeDeApplication =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule applicationNaoDependeDeInfrastructure =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule servicesSoNoApplication =
        classes().that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule entidadesSoNoDomain =
        classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..domain..");
}
```

---

## 📌 CHECKLIST DE CODE REVIEW

### Estrutura

- [ ] Arquivo de teste segue nomenclatura `*Test.java` / `*.spec.ts`
- [ ] Teste espelha a estrutura de `src/main`
- [ ] Fixtures/mocks organizados e reutilizáveis
- [ ] `@Nested` usado para agrupar cenários no JUnit 5

### Mocking

- [ ] Mocks apenas para dependências externas (ports/adapters)
- [ ] Não mocka entidades, value objects ou DTOs
- [ ] `ArgumentCaptor` quando precisa inspecionar argumentos
- [ ] WireMock para APIs externas

### Asserções

- [ ] Pelo menos uma asserção por teste
- [ ] Usa AssertJ (não `assertEquals` do JUnit)
- [ ] Testa casos de sucesso E erro
- [ ] Verifica chamadas de mock quando relevante

### Cobertura

- [ ] Domain ≥ 90%
- [ ] Application ≥ 80%
- [ ] Infrastructure ≥ 70%
- [ ] Testa edge cases e boundary values

### Boas Práticas

- [ ] Testes independentes (sem estado compartilhado)
- [ ] Determinísticos (sem `Thread.sleep`, sem `Random` sem seed)
- [ ] Padrão Arrange → Act → Assert respeitado
- [ ] Nomes descritivos que lêem como especificação

---

**🚨 LEMBRE-SE: Testes são documentação executável. Escreva-os como se outra pessoa fosse ler!**
