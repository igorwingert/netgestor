# Roteiro do vídeo 2: testes dos endpoints (Postman)

**Duração sugerida:** 15 a 20 minutos.
**Objetivo (enunciado, item j):** demonstrar autenticação, geração e uso do token JWT, cadastro, consulta/listagem,
busca por ID, atualização, exclusão, os principais fluxos e a **evidência de que os endpoints protegidos exigem autenticação**.

## Antes de gravar

- [ ] `docker compose down -v && docker compose up -d` e subir a API (banco limpo deixa os números previsíveis)
- [ ] Importar no Postman `postman/NetGestor.postman_collection.json` e `postman/NetGestor.postman_environment.json`
- [ ] Selecionar o environment **NetGestor - Local**
- [ ] Deixar a aba **Tests** e o painel do **environment** visíveis (para mostrar o token sendo salvo)

---

## 1. Visão geral da coleção (≈ 1 min)

**Mostrar:** a árvore de pastas e a aba **Authorization** da coleção (Bearer `{{token}}`, herdado por todas as requisições).

**Falar:** uma pasta por recurso, cada uma com cadastrar → listar → buscar → atualizar → consultas → testes negativos → excluir;
a pasta `00` faz o login, e a `99` executa o fluxo completo. Cada requisição tem testes (`pm.test`) que conferem
status, campos e links.

## 2. Autenticação e token JWT (≈ 3 min) — pasta `00 - Autenticação`

1. **Login ADMIN**: mostrar o corpo (`email`, `senha`), a resposta (`token`, `tipo`, `expiraEmSegundos`, `usuario`)
   e a aba **Tests**: o script salva o token no environment (`pm.environment.set("token", ...)`). Mostrar a variável preenchida.
2. Colar o token em **jwt.io** (opcional) e mostrar o payload: `sub`, `perfil`, `exp`. Não há senha no token.
3. **Login com senha inválida** → **401** com o JSON de erro padrão.
4. **Usuário logado (/auth/me)** → o token é enviado no header `Authorization: Bearer ...` (mostrar na aba Headers / Console).
5. **Evidência de proteção:**
   - **Sem token** (`GET /clientes` com "No Auth") → **401** "Autenticação necessária";
   - **Token inválido** → **401** "Token inválido";
   - mais adiante, nas pastas: **perfil sem permissão** → **403** (ex.: atendente listando usuários).

## 3. CRUD completo de um recurso (≈ 5 min) — pasta `05 - Clientes e Endereços`

Executar uma a uma, comentando o status e a resposta:

| Operação | Requisição | O que destacar |
|---|---|---|
| Cadastro | Cadastrar cliente | **201**, header **Location**, `_links` (enderecos, contratos); CPF gerado no Pre-request script |
| Validação | Cadastrar com CPF inválido / campos inválidos | **400** com a lista `campos` |
| Conflito | Cadastrar com CPF já existente | **409** com mensagem clara |
| Listagem | Listar clientes | **200**, coleção HAL em `_embedded.clientes` |
| Consulta | Consulta por nome / por CPF/CNPJ | filtros `?nome=` e `?cpfCnpj=` |
| CNPJ alfanumérico | Cadastrar PJ com CNPJ alfanumérico (V3) | o novo formato aceito; dígito errado → 400 |
| Busca por ID | Buscar cliente por ID / inexistente / ID inválido | **200** / **404** / **400** |
| Atualização | Atualizar cliente | **200** com os dados novos |
| Sub-recurso | Cadastrar/listar/atualizar/excluir endereço | `/clientes/{id}/enderecos` e `/enderecos/{id}` |
| Exclusão | Excluir cliente com contratos / Excluir cliente | **409** (regra) / **204** sem corpo |
| Conferência | Buscar cliente excluído | **404** |

## 4. Regras de negócio e HATEOAS (≈ 4 min) — pasta `06 - Contratos`

1. **Ocupação da CTO 2**: o teste guarda a primeira porta livre em `{{portaLivre}}`.
2. **Cadastrar contrato** → 201; mostrar os links `suspender` e `cancelar`.
3. Regras com **409**: porta ocupada, porta acima da capacidade, plano rádio com CTO, plano inativo, endereço de outro cliente.
4. **Suspender** → 200: mostrar que agora existe `reativar` e **não existe mais** `suspender` (testes "Tem o link"/"Não tem o link").
5. **Suspender de novo** → 409 (transição inválida).
6. **TECNICO lê o contrato** → 200, mas **sem links de ação**; **TECNICO tenta suspender** → **403**.
7. Sub-recursos: faturas, chamados, OS e equipamentos do contrato.

Mostrar rapidamente as outras pastas (Planos, OLTs, CTOs, Equipamentos, Técnicos, Chamados, OS, Faturas), destacando:
- Chamados: iniciar-atendimento → resolver → fechar (`dataFechamento` preenchida);
- OS: a **Marina** (tecnico2) tenta iniciar uma OS do Carlos → **403**; o Carlos inicia e conclui;
- Faturas: pagamento parcial (continua ABERTA) → restante (vira **PAGA**) → excluir pagamento (volta a ABERTA).

## 5. Fluxo completo (≈ 3 min) — pasta `99 - Fluxos`

Rodar a pasta pelo **Runner** e comentar: cliente → endereço → contrato → OS de instalação + ONU → chamado →
fatura → pagamento → retirada do equipamento → cancelamento do contrato → **porta da CTO liberada**.

**Falar:** as ações usam a **URL do link** recebido (ex.: `{{fluxoLinkPagar}}` veio de `_links['registrar-pagamento']`).
É um cliente HATEOAS: ele navega pela API seguindo os links, sem montar URLs de ação à mão.

## 6. Execução completa (≈ 1 min)

- Runner da coleção inteira, **ou** no terminal:
  `npx newman run postman/NetGestor.postman_collection.json -e postman/NetGestor.postman_environment.json`
- Resultado esperado: **226 requisições, 764 asserções, 0 falhas**. Rodar duas vezes seguidas para mostrar que a coleção
  é repetível (gera dados únicos e exclui o que cria).
