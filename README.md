# NetGestor: Sistema de Gestão de Provedor de Internet

Projeto do **Exame de Suficiência de Tecnologia em Desenvolvimento de Sistemas, UTFPR (2026/2)**.
Autor: Igor Wingert.

API REST em **Spring Boot** com **frontend Angular** para a gestão de um provedor de internet: clientes, planos,
contratos, rede de fibra óptica (OLT e CTO), equipamentos em comodato, chamados de atendimento, ordens de serviço
de campo, faturas e pagamentos. Possui autenticação **JWT** com três perfis de acesso e respostas **HATEOAS (HAL)**,
seguindo o **nível 3 do Modelo de Maturidade de Richardson**.

---

## Sumário

1. [Sobre o sistema](#1-sobre-o-sistema)
2. [Tecnologias](#2-tecnologias)
3. [Estrutura do repositório e arquitetura](#3-estrutura-do-repositório-e-arquitetura)
4. [Configuração do banco de dados](#4-configuração-do-banco-de-dados)
5. [Como executar o backend](#5-como-executar-o-backend)
6. [Como executar o frontend](#6-como-executar-o-frontend)
7. [Usuários iniciais](#7-usuários-iniciais)
8. [Autenticação e uso do token JWT](#8-autenticação-e-uso-do-token-jwt)
9. [Perfis e permissões](#9-perfis-e-permissões)
10. [Endpoints](#10-endpoints)
11. [Modelo de Maturidade de Richardson e HATEOAS](#11-modelo-de-maturidade-de-richardson-e-hateoas)
12. [Integração entre frontend e API](#12-integração-entre-frontend-e-api)
13. [Testes](#13-testes)
14. [Decisões técnicas](#14-decisões-técnicas)
15. [Referências](#15-referências)
16. [Declaração de uso de ferramentas de IA](#16-declaração-de-uso-de-ferramentas-de-ia)

---

## 1. Sobre o sistema

Um provedor de internet (ISP) precisa controlar tanto a parte comercial quanto a técnica do serviço. O NetGestor cobre:

- **Comercial:** clientes (pessoa física ou jurídica) e seus endereços, planos de internet (fibra ou rádio) e contratos.
- **Rede:** OLTs (equipamento central), CTOs (caixas na rua) e a **porta da CTO** usada por cada contrato de fibra.
- **Campo:** equipamentos (ONU, roteador) em estoque ou em comodato com o cliente, técnicos e ordens de serviço.
- **Atendimento:** chamados com ciclo de vida (aberto → em atendimento → resolvido → fechado).
- **Financeiro:** faturas mensais e pagamentos (inclusive parciais).

### Entidades (13)

| Entidade | Descrição |
|---|---|
| Usuario | Login do sistema; perfil ADMIN, ATENDENTE ou TECNICO; senha com hash BCrypt |
| Cliente | Pessoa física (CPF) ou jurídica (CNPJ, inclusive alfanumérico) |
| Endereco | Endereços do cliente; usado como endereço de instalação do contrato |
| Plano | Velocidades, mensalidade e tecnologia (FIBRA ou RADIO) |
| Olt | Equipamento central da rede óptica |
| Cto | Caixa de terminação óptica com N portas |
| Contrato | Cliente + plano + endereço + porta da CTO; status ATIVO/SUSPENSO/CANCELADO |
| Equipamento | ONU ou roteador; ESTOQUE, INSTALADO (comodato) ou DEFEITO |
| Tecnico | Técnico de campo, ligado 1:1 a um usuário |
| Chamado | Atendimento com protocolo, prioridade e status |
| OrdemServico | Serviço de campo (instalação, reparo, retirada...); N:N com equipamentos |
| Fatura | Cobrança mensal de um contrato |
| Pagamento | Pagamento (total ou parcial) de uma fatura |

O diagrama entidade-relacionamento completo, com o mapeamento JPA de cada relação, está em **[docs/DER.md](docs/DER.md)**.

![DER](docs/der.png)

### Principais regras de negócio

- Cliente/Plano/OLT/CTO com dependentes não podem ser excluídos → **409** com mensagem explicando o motivo.
- CPF/CNPJ (com validação dos dígitos verificadores), e-mail, MAC, número de série e IP duplicados → **409**.
- Contrato de fibra exige CTO e porta; a porta precisa estar livre e dentro da capacidade da CTO. Plano via rádio não usa CTO.
- Contrato: `suspender` (só ATIVO), `reativar` (só SUSPENSO), `cancelar` (se não estiver cancelado). Cancelar libera a porta.
- Chamado: `iniciar-atendimento` → `resolver` → `fechar` (preenche `dataFechamento`).
- Ordem de serviço: `iniciar` → `concluir` (preenche `dataConclusao`); o técnico só altera **as próprias** OS.
- Equipamento vinculado a um contrato fica INSTALADO; ao desvincular, volta para ESTOQUE.
- Fatura passa a PAGA quando a soma dos pagamentos alcança o valor; volta a ABERTA/VENCIDA se um pagamento for excluído.

---

## 2. Tecnologias

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 4.1.2 (Web MVC, Data JPA/Hibernate 7, Validation, Security 7, HATEOAS), Lombok, Maven Wrapper |
| Segurança | JWT com JJWT 0.12.6, BCrypt, `@EnableMethodSecurity` / `@PreAuthorize` |
| Banco de dados | PostgreSQL 16 (Docker Compose), Flyway 12 (migrations V1, V2 e V3) |
| Frontend | Angular 22.2 (standalone components, signals, Reactive Forms, HttpClient com interceptor), Bootstrap 5.3 e Bootstrap Icons |
| Testes | JUnit 5 + AssertJ + MockMvc (24 testes); Postman (coleção v2.1) executada com Newman |

---

## 3. Estrutura do repositório e arquitetura

```
netgestor/
├── backend/                 API Spring Boot
│   └── src/main/java/br/edu/utfpr/netgestor/
│       ├── config/          CORS
│       ├── security/        SecurityConfig, JwtService, JwtAuthFilter, handlers 401/403, UserDetailsService
│       ├── domain/          13 entidades JPA + enums (com as regras de transição de status)
│       ├── repository/      interfaces Spring Data JPA (métodos derivados e @Query JPQL)
│       ├── service/         regras de negócio (@Transactional)
│       ├── controller/      controllers REST
│       ├── dto/             records de entrada (Request) e saída (Response) + validação personalizada
│       ├── assembler/       RepresentationModelAssemblers (links HATEOAS)
│       └── exception/       exceções de negócio + @RestControllerAdvice (formato único de erro)
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/    V1__criar_tabelas.sql, V2__dados_iniciais.sql, V3__cnpj_alfanumerico.sql
├── frontend/                Angular
│   └── src/app/
│       ├── core/            auth (service, interceptor, guards), models, layout, HateoasService, ToastService
│       ├── features/        clientes, planos, contratos, chamados, ordens-servico, login, inicio
│       └── shared/          componentes, pipe e utilitários reutilizáveis
├── postman/                 coleção + environment (v2.1)
├── docs/                    DER e roteiros dos vídeos
└── docker-compose.yml       PostgreSQL
```

Fluxo de uma requisição na API (arquitetura em camadas):

```
HTTP/JSON → Controller → Request DTO (Bean Validation) → Service (regras + transação) → Repository → PostgreSQL
HTTP/HAL  ← Controller ← Assembler (links) ← Response DTO
```

---

## 4. Configuração do banco de dados

Pré-requisito: **Docker** (Docker Desktop no Windows).

```bash
docker compose up -d        # sobe o PostgreSQL 16 em localhost:5432 (banco/usuário/senha: netgestor)
docker compose down         # para (os dados ficam no volume)
docker compose down -v      # apaga o volume: na próxima subida da API o Flyway recria tudo do zero
```

**Não é preciso criar tabelas manualmente.** Ao iniciar, a API executa o Flyway, que aplica as migrations pendentes:

| Migration | Conteúdo |
|---|---|
| `V1__criar_tabelas.sql` | Estrutura: 14 tabelas, PKs, FKs, UNIQUE, CHECK e índices |
| `V2__dados_iniciais.sql` | Dados de teste: usuários, planos, rede, clientes, contratos, equipamentos, chamados, OS, faturas e pagamentos |
| `V3__cnpj_alfanumerico.sql` | Evolução: aceita o CNPJ alfanumérico da Receita Federal |

O Hibernate roda com `ddl-auto: validate`: ele **não altera** o banco, apenas confere se as entidades conferem com as tabelas.

### Variáveis de ambiente (todas têm valor padrão para desenvolvimento)

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/netgestor` | URL do banco |
| `DB_USER` / `DB_PASSWORD` | `netgestor` / `netgestor` | Credenciais do banco |
| `JWT_SECRET` | chave de desenvolvimento (≥ 32 caracteres) | Chave HMAC de assinatura dos tokens |
| `JWT_EXPIRATION` | `7200000` (2 horas, em ms) | Validade do token |

---

## 5. Como executar o backend

Pré-requisitos: **Java 21** e o banco rodando (seção 4). O Maven não precisa estar instalado (usa o wrapper).

```bash
cd backend
./mvnw spring-boot:run      # Windows (cmd/PowerShell): mvnw.cmd spring-boot:run
```

A API sobe em **http://localhost:8080/api**. Para rodar os testes automatizados (o banco precisa estar no ar):

```bash
./mvnw verify
```

---

## 6. Como executar o frontend

Pré-requisitos: **Node.js 22.22+ ou 24.15+** e a API rodando (seção 5).

```bash
cd frontend
npm install
npm start                   # http://localhost:4200
```

Build de produção: `npm run build` (gera `frontend/dist/`).

---

## 7. Usuários iniciais

Criados pela migration V2 (as senhas estão gravadas como hash BCrypt):

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | `admin@netgestor.com` | `admin123` |
| ATENDENTE | `atendente@netgestor.com` | `atendente123` |
| TECNICO | `tecnico@netgestor.com` (Carlos Souza) | `tecnico123` |
| TECNICO | `tecnico2@netgestor.com` (Marina Lopes) | `tecnico123` |

O segundo técnico existe para demonstrar que um técnico não pode alterar a ordem de serviço de outro (403).
A tela de login do frontend tem botões que preenchem esses usuários.

---

## 8. Autenticação e uso do token JWT

**1. Login** (único endpoint público):

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "atendente@netgestor.com", "senha": "atendente123" }
```

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhdGVuZGV...",
  "tipo": "Bearer",
  "expiraEmSegundos": 7200,
  "usuario": { "id": 2, "nome": "Juliana Ramos", "email": "atendente@netgestor.com", "perfil": "ATENDENTE", "ativo": true }
}
```

**2. Uso:** enviar o token em todas as requisições seguintes:

```http
GET /api/contratos
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

`GET /api/auth/me` devolve os dados do usuário dono do token.

**Como funciona:**

- O token é assinado com **HMAC-SHA** usando `JWT_SECRET`. O payload contém `sub` (e-mail), `id`, `nome`, `perfil`,
  `iat` e `exp`. Ele pode ser lido por qualquer um (Base64), mas qualquer alteração invalida a assinatura. Por isso
  **não contém a senha**.
- O `JwtAuthFilter` (`OncePerRequestFilter`) valida o token em cada requisição e recarrega o usuário do banco: um usuário
  desativado perde o acesso imediatamente.
- A API é **stateless** (sem sessão nem cookies).
- Erros de segurança usam o mesmo JSON de erro do resto da API:

```json
{ "timestamp": "2026-10-27T00:17:56", "status": 401, "erro": "Unauthorized",
  "mensagem": "Autenticação necessária: envie o header Authorization: Bearer <token>", "caminho": "/api/contratos", "campos": [] }
```

| Situação | Status | Mensagem |
|---|---|---|
| Sem token | 401 | Autenticação necessária... |
| Token inválido ou adulterado | 401 | Token inválido |
| Token expirado | 401 | Token expirado: faça login novamente |
| E-mail ou senha errados | 401 | E-mail ou senha inválidos |
| Perfil sem permissão | 403 | Seu perfil não tem permissão para esta operação |

---

## 9. Perfis e permissões

Aplicadas com `@PreAuthorize` em cada operação. O frontend esconde o que o perfil não pode usar, mas **quem protege é a API**.

| Recurso | ADMIN | ATENDENTE | TECNICO |
|---|---|---|---|
| Usuários | CRUD | — | — |
| Planos | CRUD | leitura | — |
| OLTs | CRUD | — | leitura |
| CTOs e ocupação | CRUD | leitura | leitura |
| Clientes e endereços | CRUD | CRUD | — |
| Contratos | CRUD + ações | CRUD + ações | leitura |
| Chamados | CRUD + ações | CRUD + ações | — |
| Faturas e pagamentos | CRUD + ações | CRUD + ações | — |
| Equipamentos | CRUD + vincular | — | CRUD + vincular |
| Técnicos | CRUD | leitura | leitura |
| Ordens de serviço | tudo | agendar, editar, cancelar | ver, iniciar e concluir **as próprias** |

---

## 10. Endpoints

Todos sob `/api` e protegidos por JWT, exceto o login. Cada recurso tem o CRUD padrão:
`GET /recurso` (lista), `GET /recurso/{id}`, `POST /recurso` (201 + Location), `PUT /recurso/{id}`, `DELETE /recurso/{id}` (204).

| Recurso | Consultas específicas, sub-recursos e ações |
|---|---|
| `/auth` | `POST /login`, `GET /me` |
| `/usuarios` | — |
| `/clientes` | `?nome=&cpfCnpj=` · `GET/POST /{id}/enderecos` · `GET /{id}/contratos` |
| `/enderecos` | `GET/PUT/DELETE /{id}` |
| `/planos` | — |
| `/olts` | `GET /{id}/ctos` |
| `/ctos` | `GET /{id}/ocupacao` (portas livres e ocupadas) |
| `/contratos` | `?status=&clienteId=` · `GET /{id}/faturas`, `/chamados`, `/ordens-servico`, `/equipamentos` · `PATCH /{id}/suspender`, `/reativar`, `/cancelar` |
| `/equipamentos` | `?status=&tipo=` · `PATCH /{id}/vincular` (`{ "contratoId": 1 }`), `/desvincular` |
| `/tecnicos` | `GET /{id}/ordens-servico?status=` (agenda do técnico) |
| `/chamados` | `?status=&prioridade=` · `PATCH /{id}/iniciar-atendimento`, `/resolver`, `/fechar` |
| `/ordens-servico` | `?status=&tecnicoId=` · `PATCH /{id}/iniciar`, `/concluir`, `/cancelar` |
| `/faturas` | `?status=VENCIDA&competencia=2026-10` · `GET/POST /{id}/pagamentos` · `PATCH /{id}/cancelar` |
| `/pagamentos` | `GET/PUT/DELETE /{id}` |

Exemplos de chamada de todos os endpoints estão na coleção do Postman (seção 13).

---

## 11. Modelo de Maturidade de Richardson e HATEOAS

O modelo de Leonard Richardson classifica APIs em quatro níveis. O NetGestor atinge o **nível 3**:

### Nível 0: um único endpoint

Não se aplica: a API não usa um endpoint genérico para todas as operações.

### Nível 1: recursos

Cada recurso tem a sua própria URI, no plural, inclusive sub-recursos:
`/api/clientes/1`, `/api/clientes/1/enderecos`, `/api/contratos/2/faturas`, `/api/faturas/7/pagamentos`.

### Nível 2: verbos e códigos HTTP

| Operação | Verbo | Resposta |
|---|---|---|
| Listar / buscar | `GET` | 200 (ou 404 se o id não existe) |
| Cadastrar | `POST` | **201 Created** + header `Location` + corpo |
| Atualizar | `PUT` | 200 |
| Ação de mudança de estado | `PATCH /{id}/acao` | 200 com o recurso atualizado |
| Excluir | `DELETE` | **204 No Content** |
| Dados inválidos | — | **400** com a lista de erros por campo |
| Regra de negócio violada | — | **409 Conflict** |
| Sem token / sem permissão | — | **401** / **403** |

```http
POST /api/chamados  →  HTTP/1.1 201
Location: http://localhost:8080/api/chamados/11
Content-Type: application/hal+json
```

```json
{ "status": 400, "erro": "Bad Request", "mensagem": "Existem campos inválidos na requisição", "caminho": "/api/clientes",
  "campos": [ { "campo": "nome", "mensagem": "O nome é obrigatório" },
              { "campo": "cpfCnpj", "mensagem": "CNPJ inválido para pessoa jurídica" } ] }
```

```json
{ "status": 409, "erro": "Conflict", "caminho": "/api/contratos/6/cancelar",
  "mensagem": "Ação 'cancelar' não permitida para o contrato CT-2024-0001 com status CANCELADO", "campos": [] }
```

### Nível 3: controles de hipermídia (HATEOAS)

As respostas usam o formato **HAL** (`application/hal+json`), produzido por um `RepresentationModelAssembler` por recurso.
Cada recurso traz `_links` para si mesmo, para a coleção e para os recursos relacionados, e **as ações disponíveis
mudam conforme o estado do recurso e o perfil do usuário**.

Contrato **ATIVO**, visto pela atendente (resposta real, `GET /api/contratos/2`):

```json
{
  "id": 2,
  "numero": "CT-2025-0002",
  "clienteNome": "Maria Oliveira",
  "planoNome": "Fibra 300 Mega",
  "ctoIdentificacao": "CTO-CEN-01",
  "portaCto": 2,
  "status": "ATIVO",
  "_links": {
    "self":               { "href": "http://localhost:8080/api/contratos/2" },
    "contratos":          { "href": "http://localhost:8080/api/contratos" },
    "cliente":            { "href": "http://localhost:8080/api/clientes/2" },
    "plano":              { "href": "http://localhost:8080/api/planos/1" },
    "enderecoInstalacao": { "href": "http://localhost:8080/api/enderecos/3" },
    "cto":                { "href": "http://localhost:8080/api/ctos/1" },
    "faturas":            { "href": "http://localhost:8080/api/contratos/2/faturas" },
    "chamados":           { "href": "http://localhost:8080/api/contratos/2/chamados" },
    "ordensServico":      { "href": "http://localhost:8080/api/contratos/2/ordens-servico" },
    "equipamentos":       { "href": "http://localhost:8080/api/contratos/2/equipamentos" },
    "suspender":          { "href": "http://localhost:8080/api/contratos/2/suspender" },
    "cancelar":           { "href": "http://localhost:8080/api/contratos/2/cancelar" }
  }
}
```

Depois de `PATCH /api/contratos/2/suspender`, a resposta traz `status: "SUSPENSO"` e os links de ação mudam:
**`suspender` some e aparece `reativar`**. Um contrato CANCELADO não traz nenhum link de ação. O mesmo contrato visto
por um **TECNICO** também não traz links de ação, nem links para recursos que ele não pode acessar (cliente, faturas...).

| Recurso | Links de ação conforme o estado |
|---|---|
| Contrato | ATIVO → `suspender`, `cancelar` · SUSPENSO → `reativar`, `cancelar` · CANCELADO → nenhum |
| Chamado | ABERTO → `iniciar-atendimento` · EM_ATENDIMENTO → `resolver` · RESOLVIDO → `fechar` |
| Ordem de serviço | AGENDADA → `iniciar`, `cancelar` · EM_EXECUCAO → `concluir`, `cancelar` |
| Equipamento | ESTOQUE → `vincular` · INSTALADO → `desvincular` |
| Fatura | ABERTA/VENCIDA → `registrar-pagamento`, `cancelar` · PAGA/CANCELADA → nenhum |

Coleções trazem os itens em `_embedded` e um link `self` com os filtros aplicados:

```json
{
  "_embedded": { "contratos": [ { "id": 4, "numero": "CT-2026-0001", "status": "SUSPENSO", "...": "...",
                                  "_links": { "self": { "href": ".../api/contratos/4" }, "reativar": { "href": ".../api/contratos/4/reativar" } } } ] },
  "_links": { "self": { "href": "http://localhost:8080/api/contratos?status=SUSPENSO" } }
}
```

**Onde está no código:** `assembler/*Assembler.java` (montagem dos links), `domain/enums/Status*.java` (métodos
`podeSuspender()`, `podeResolver()`... usados pelo service para responder 409 **e** pelo assembler para decidir os links),
`security/Permissoes.java` (filtro dos links por perfil).

---

## 12. Integração entre frontend e API

- **Endereço da API:** `frontend/src/app/core/api.config.ts` (`http://localhost:8080/api`). O backend libera CORS para
  `http://localhost:4200` (`config/CorsConfig.java`) e expõe o header `Location`.
- **Login e token:** `AuthService` chama `POST /auth/login`, guarda o token e o usuário no `localStorage` e descarta o
  token expirado ao abrir a aplicação (lê o `exp` do payload).
- **Interceptor** (`auth.interceptor.ts`): acrescenta `Authorization: Bearer <token>` em toda chamada à API e, se receber
  **401**, faz logout e redireciona para o login.
- **Guards:** `autenticadoGuard` (exige login) e `perfilGuard` (cada rota declara os perfis com acesso); o menu também é
  montado conforme o perfil.
- **Services por recurso** (`cliente.service.ts`, `contrato.service.ts`, ...) leem as coleções HAL em `_embedded`.
- **HATEOAS no frontend:** os botões de ação (suspender, reativar, iniciar atendimento, concluir OS...) são criados a partir
  dos `_links` da resposta, e o clique faz `PATCH` no `href` do link (`HateoasService`). As listas relacionadas do contrato
  (faturas, chamados...) são carregadas seguindo os links. O frontend não duplica as regras de transição.
- **Respostas:** sucesso → toast; **400** → mensagem embaixo de cada campo do formulário; **409/403/404** → toast com a
  `mensagem` da API; API fora do ar → aviso específico.

Telas: login; início; clientes (CRUD + endereços); planos (CRUD para ADMIN); contratos (lista, cadastro/edição com
escolha de porta livre, detalhe com ações); chamados (abertura, filtros e mudança de status); ordens de serviço
(agenda do técnico, iniciar/concluir).

---

## 13. Testes

### Testes automatizados do backend (`./mvnw verify`)

24 testes (o PostgreSQL precisa estar rodando):

- `MapeamentoJpaTest`: lê os dados da V2 navegando por cada tipo de relacionamento JPA e confere os hashes BCrypt;
- `ConsultasJpaTest`: consultas com filtros opcionais (inclusive CNPJ alfanumérico);
- `SegurancaIntegrationTest` (MockMvc): login, 401, token adulterado, 403 por perfil, links por perfil, OS de outro técnico;
- `DocumentoValidoValidatorTest`: dígitos verificadores de CPF e CNPJ (numérico e alfanumérico);
- `NetgestorApplicationTests`: o contexto da aplicação sobe (inclui Flyway + validação do schema).

### Coleção do Postman (`postman/`)

- `NetGestor.postman_collection.json` (v2.1) e `NetGestor.postman_environment.json` (`baseUrl`, tokens e ids).
- **226 requisições e 764 asserções** (`pm.test`), organizadas em pastas: `00 - Autenticação` (login dos perfis, com
  script que salva o token), uma pasta por recurso (cadastrar, listar, buscar por ID, atualizar, excluir e consultas) com
  testes negativos (400, 401, 403, 404, 409), e `99 - Fluxos` (ciclo completo do cliente seguindo os links HATEOAS).
- A autorização `Bearer {{token}}` é herdada da coleção. Os dados únicos (CPF, CNPJ, e-mail, MAC, IP) são gerados a cada
  execução e cada pasta exclui o que cria, então a coleção pode ser executada várias vezes seguidas.

Com o banco e a API rodando:

```bash
npx newman run postman/NetGestor.postman_collection.json -e postman/NetGestor.postman_environment.json
```

No Postman: importar os dois arquivos, selecionar o environment **NetGestor - Local** e executar a coleção pelo **Runner**.

---

## 14. Decisões técnicas

- **Flyway + `ddl-auto: validate`:** o schema é versionado e revisável; a V3 mostra uma evolução sem alterar migrations já aplicadas.
- **DTOs em `record`** separados das entidades: a API nunca expõe a entidade (nem a senha) e a validação fica na entrada.
- **Regras em duas camadas:** o service valida e devolve 409 com mensagem clara; o banco garante as mesmas regras com
  UNIQUE, CHECK e um **índice único parcial** (porta da CTO livre quando o contrato é cancelado).
- **Uma única fonte para as transições de status** (métodos nos enums), usada pela regra (409) e pelos links HATEOAS.
- **`@ManyToOne(fetch = LAZY)` + `@EntityGraph`** nas listagens para evitar o problema N+1; `open-in-view: false`.
- **Formato único de erro** (`@RestControllerAdvice` + handlers de segurança).
- **JWT stateless**, usuário recarregado do banco a cada requisição (desativação imediata); links filtrados por perfil.
- **Frontend com signals** (Angular 22 usa OnPush/zoneless por padrão), lazy loading das telas e cancelamento da busca
  anterior ao filtrar (evita que uma resposta antiga sobrescreva a lista filtrada).

---

## 15. Referências

- Materiais da disciplina de Tecnologia em Desenvolvimento de Sistemas, UTFPR. **[Igor: completar com os materiais/aulas utilizados]**
- Spring Boot Reference Documentation: https://docs.spring.io/spring-boot/
- Spring Data JPA Reference: https://docs.spring.io/spring-data/jpa/reference/
- Spring Security Reference: https://docs.spring.io/spring-security/reference/
- Spring HATEOAS Reference: https://docs.spring.io/spring-hateoas/docs/current/reference/html/
- Flyway Documentation: https://documentation.red-gate.com/flyway
- JJWT (Java JWT): https://github.com/jwtk/jjwt
- RFC 7519, JSON Web Token (JWT): https://www.rfc-editor.org/rfc/rfc7519
- JSON Hypertext Application Language (HAL): https://datatracker.ietf.org/doc/html/draft-kelly-json-hal
- FOWLER, Martin. *Richardson Maturity Model*: https://martinfowler.com/articles/richardsonMaturityModel.html
- Angular Documentation: https://angular.dev
- Bootstrap 5.3 Documentation: https://getbootstrap.com/docs/5.3/
- Postman Learning Center: https://learning.postman.com/docs/ · Newman: https://github.com/postmanlabs/newman
- Receita Federal do Brasil. Instrução Normativa RFB nº 2.229/2024 (CNPJ alfanumérico).

---

## 16. Declaração de uso de ferramentas de IA

Este projeto foi desenvolvido com o apoio do **Claude Code** (assistente de programação da Anthropic, modelo Claude Opus 5.5),
usado como ferramenta de auxílio, conforme permitido pelo enunciado. A ferramenta foi utilizada para:

- sugerir e escrever código do backend, do frontend, das migrations e da coleção do Postman, seguindo o planejamento
  em etapas definido pelo autor (arquivo `CLAUDE.md`);
- executar e verificar os testes (Maven, Newman e testes no navegador) e apontar/corrigir defeitos encontrados;
- redigir a documentação (este README, o DER e os roteiros dos vídeos).

As decisões de domínio, modelagem e arquitetura foram definidas pelo autor, que revisou o código produzido e é capaz de
explicar a solução, como demonstrado nos vídeos de apresentação. O histórico de commits (um por etapa) registra a evolução do projeto.
