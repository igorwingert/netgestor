# Roteiro do vídeo 3: código-fonte e funcionamento do frontend

**Duração sugerida:** 15 a 20 minutos.
**Objetivo (enunciado, item k):** mostrar a estrutura do projeto Angular, as telas, os serviços que falam com a API,
a autenticação (armazenamento e envio do token), o código de cadastro/listagem/atualização/exclusão, **e** a aplicação
rodando: login, fluxos principais consumindo a API de verdade, e o tratamento de respostas de sucesso e erro.
**É preciso mostrar o código E a execução.**

## Antes de gravar

- [ ] Banco limpo + API rodando; `cd frontend && npm start` (http://localhost:4200)
- [ ] No navegador, abrir o **DevTools** na aba **Network** (filtro `Fetch/XHR`) para mostrar as chamadas reais à API
- [ ] VS Code aberto em `frontend/src/app`

---

## 1. Estrutura do projeto (≈ 2 min)

**Mostrar:** `frontend/src/app`.

**Falar:**
- `core/`: o que é usado pela aplicação inteira: autenticação (`auth/`), modelos (`models/`), layout/menu,
  `HateoasService` e `ToastService`.
- `features/<recurso>/`: cada módulo com seu **service** (comunicação com a API) e suas telas (lista, formulário, detalhe).
- `shared/`: peças reutilizáveis: toasts, badge de status, pipe de CPF/CNPJ, tratamento de erros da API.
- `app.routes.ts`: rotas com **guards** e **lazy loading**. `app.config.ts`: `HttpClient` com o interceptor, locale pt-BR.
- Angular 22: componentes standalone, `inject()`, `@if/@for` no template. A tela é atualizada por **signals**
  (no Angular 22 o padrão é OnPush/zoneless), por isso todo estado de tela é `signal()`.

## 2. Comunicação com a API (≈ 2 min)

**Mostrar:** `features/clientes/cliente.service.ts`, `core/models/hal.model.ts`, `core/hateoas.service.ts`.

**Falar:**
- Um service por recurso; os componentes nunca usam o `HttpClient` direto.
- A API responde em HAL: a lista vem em `_embedded.clientes` → função `itensDaColecao()`.
- `HateoasService`: `executar(link)` faz PATCH no `href` de um link de ação; `seguirColecao(link)` busca uma coleção relacionada.

## 3. Autenticação no frontend (≈ 3 min)

**Mostrar:** `core/auth/auth.service.ts`, `auth.interceptor.ts`, `auth.guards.ts`, `features/login/`.

**Falar:**
- `login()` chama `POST /api/auth/login` e guarda o **token no localStorage** junto com os dados do usuário.
  `usuario` é um signal: menu, guards e telas reagem ao login/logout.
- Ao abrir a aplicação, o token salvo é descartado se estiver **expirado** (o `exp` é lido do payload do JWT).
- O **interceptor** acrescenta `Authorization: Bearer <token>` em toda requisição à API e, se vier **401**,
  faz logout e volta para o login.
- **Guards**: `autenticadoGuard` (precisa estar logado) e `perfilGuard` (cada rota declara `data.perfis`).

**Executar:**
1. Login com senha errada → mensagem "E-mail ou senha inválidos" (veio da API).
2. Login como **ATENDENTE** → mostrar no DevTools: **Application → Local Storage** com o token, e na aba **Network**
   o header `Authorization: Bearer ...` nas chamadas seguintes.
3. Mostrar o menu montado conforme o perfil.

## 4. CRUD de clientes: código e execução (≈ 4 min)

**Mostrar o código:** `cliente-lista.component.ts` (listar, filtrar, excluir) e `cliente-form.component.ts`
(cadastrar/atualizar, endereços), e a função `aplicarErrosNoFormulario` em `shared/utils/erros-api.ts`.

**Executar:**
1. **Listagem** e **filtro** por nome (mostrar na Network: `GET /api/clientes?nome=...`).
2. **Cadastro com CPF inválido** → o erro **400 da API aparece embaixo do campo** CPF.
3. Corrigir e salvar → **201**, toast de sucesso, tela de edição para cadastrar **endereços** (sub-recurso).
4. Cadastrar um endereço; **editar** o cliente (PUT).
5. **Excluir** um cliente com contrato (ex.: Padaria) → toast com a mensagem do **409**; excluir um sem contrato → sucesso.
6. Opcional: cadastrar uma PJ com CNPJ alfanumérico `12.ABC.345/01DE-35`… (já existe → 409) para mencionar a V3.

## 5. Contratos e HATEOAS na tela (≈ 3 min) — destaque do vídeo

**Mostrar o código:** `contrato-detalhe.component.ts`: `ACOES_CONTRATO.filter(acao => contrato._links[acao.rel])`
e `carregarRelacionados()` (segue os links `faturas`, `chamados`...). Depois `contrato-form.component.ts`
(endereços do cliente escolhido, plano rádio sem CTO, portas livres via `/ctos/{id}/ocupacao`).

**Executar:**
1. **Novo contrato**: escolher cliente → os endereços dele aparecem; escolher CTO → só portas livres.
2. No detalhe: botões **Suspender** e **Cancelar** (vieram dos `_links`). Clicar em Suspender → o badge muda e o botão
   vira **Reativar**, sem nenhuma regra no frontend: a tela só seguiu os links da resposta.
3. Abrir o contrato `CT-2025-0001`: as seções de faturas, chamados, OS e equipamentos foram buscadas seguindo os links.

## 6. Chamados e ordens de serviço (≈ 3 min)

**Executar:**
1. **Abrir chamado** (atendente) → toast com o protocolo gerado pela API.
2. Filtrar por status/prioridade; avançar o chamado: **Iniciar atendimento → Resolver → Fechar** (botões vindos dos links).
3. Sair e entrar como **TECNICO**: menu reduzido; tentar abrir `/clientes` pela URL → bloqueado pelo guard.
4. Abrir um contrato como técnico → **sem botões de ação** (a API não mandou os links para esse perfil).
5. **Minhas ordens de serviço**: iniciar e concluir uma OS.

## 7. Tratamento de respostas (≈ 1 min)

Recapitular, mostrando `ToastService.erroDaApi` e `mensagemDeErro`:
- sucesso → toast verde;
- 400 → mensagem embaixo de cada campo;
- 409/403/404 → toast com a `mensagem` da API;
- 401 → interceptor faz logout (demonstrar apagando/alterando o token no Local Storage e navegando);
- API fora do ar → "Não foi possível conectar à API".
