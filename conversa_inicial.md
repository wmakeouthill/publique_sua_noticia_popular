esse repo, servirá pra uma aplicacao feita do zero, seguindo as regras-desenvolvimento-java-angular.

O propósito, é uma aplicação escalável, de um portal de noticias populares, que funcionará com inteligencia artificial, a pessoa poderá logar com google oauth, editar as proprias noticias como se edita no notion com /slash command, como feito na edicao de board nesse app: D:\planning_poker

E no portal, terá um feed, com as noticias mais recentes de diversas categorias, que o usuario ira definir a categoria em uma lista predefinida de categorias que o admin (eu) poderei cadastrar na plataforma, mas já deve vir com seed no banco de categorias populares pra um portal de noticias.

pra criar notícia deve ser um modal, onde em uma parte, o usuario poderá criar imagem com nano banana mais recente via api gemini com prompt no modal e ver o preview, ou upar a imagem dele, e a noticia logo embaixo no mesmo modal, um prompt, o modal deverá exibir o preview da imagem com o texto (noticia completa) a direita do formulario onde faz isso, pra ver o preview da noticia como ficará a direita, se ele nao gostar, logo abaixo do prompt que criou a noticia, deve ter um espaco pra editar a noticia tambem com a ajuda da i.a se ela nao gostou de algo, e ao postar, vai ser postada vinculada aquela conta, pra ela poder editar ou excluir posteriormente, so admin e o proprio usuario pode editar / excluir.

esse será o mvp, deverá tudo ser responsivo no front, escalavel no backend seguindo as regras de desenvolvimento.

o backend e banco ira rodar em uma instancia oracle 1gb de ram inicialmente, entao swap bem configurado, tudo bem otimizado no java / jdk e etc, e na imagem docker limpa e o mais leve possível, o front irei upar na vercel mesmo futuramente, mas deve rodar o front backend e banco local com docker compose local.

crie o plano completo pra implementação completa em etapas bem definidas e bem detalhadas seguindo as regras-desenvolvimento-java-angular.


Vou começar lendo as regras de desenvolvimento e analisando os recursos mencionados para criar o plano completo.

Analyzed
Analyzed
d:\publique_sua_noticia_popular
5 results
38 results
Agora vou ler todas as regras de desenvolvimento e examinar o editor slash command do planning_poker:

Analyzed
Analyzed
Analyzed
Analyzed
Agora vou examinar o editor slash command do planning_poker e ler o restante dos arquivos de regras:

Analyzed
Analyzed
6 results
Vou examinar o componente slash command do planning_poker para referência:

Analyzed
Analyzed
Analyzed
Analyzed
Agora tenho todas as informações necessárias. Vou examinar rapidamente o docker-compose do planning_poker como referência de estrutura:

Analyzed
Agora tenho toda a informação necessária. Vou criar o plano completo de implementação:

The model's generation exceeded the maximum output token limit.

Vou criar o plano como um artifact para não exceder o limite de tokens na resposta.

Created
Plano criado! Aqui está o resumo do que foi definido:

📰 Plano de Implementação — Portal de Notícias Populares
O plano completo está em 
plano_implementacao_portal_noticias.md
 e cobre 10 etapas seguindo rigorosamente as regras-desenvolvimento-java-angular:

Etapa	Conteúdo
1	Infraestrutura Docker (otimizada para 1GB RAM), scaffolding backend/frontend, migrações Liquibase
2	Autenticação Google OAuth2 + JWT (refresh token, SecurityConfig)
3	Categorias — CRUD admin + seed de 23 categorias populares
4	Notícias — CRUD + Editor Slash Command (baseado no planning_poker)
5	Modal de criação com IA — Gemini para imagem (Imagen 3) + texto, preview split-view
6	Feed público paginado com filtro por categoria + infinite scroll
7	Área do usuário — minhas notícias, editar/excluir
8	Área admin — dashboard, moderação, gestão de usuários
9	Responsividade + polish (dark mode, animações, skeleton, Google Fonts)
10	Testes completos + otimização Docker para produção
Destaques técnicos:
Backend: Clean Architecture + DDD, Java 25 LTS, entidades ricas, Use Cases focados, Ports/Adapters, @RequiredArgsConstructor, records para DTOs
Frontend: Angular 20+, Standalone, Signals (signal(), computed(), input(), output()), @if/@for/@defer, OnPush, composables
JVM otimizada para 1GB: G1GC, MaxRAMPercentage=60%, imagem eclipse-temurin:25-jre-alpine
Editor slash command adaptado do planning_poker com blocos extras (imagem)
21 endpoints da API mapeados com níveis de permissão
Quer que eu comece a implementação pela Etapa 1 (infraestrutura + scaffolding)?

