# Roteiro do vídeo 1: arquitetura e implementação

**Duração sugerida:** 15 a 20 minutos.
**Objetivo (enunciado, item i):** explicar a ideia, o domínio, as entidades e relacionamentos, a organização do Spring Boot,
as camadas, o Spring Data JPA, o Flyway, a segurança JWT, o Modelo de Richardson/HATEOAS, a integração com o frontend
e as decisões técnicas. **Explicar, não só mostrar arquivos.**

## Antes de gravar

- [ ] `docker compose down -v && docker compose up -d` (banco limpo, com os dados da V2/V3)
- [ ] API rodando (`cd backend && ./mvnw spring-boot:run`)
- [ ] Abrir no VS Code: `docs/der.png`, `backend/src/main/java/...`, `db/migration/`
- [ ] Deixar um terminal pronto com `curl` ou o Postman aberto para mostrar uma resposta HAL

---

## 1. Ideia e domínio (≈ 2 min)

**Mostrar:** o README (seção "Sobre o sistema").

**Falar:**
- NetGestor é um sistema de gestão para um **provedor de internet** (ISP): cadastro de clientes, planos, contratos,
  a rede de fibra (OLT → CTO → porta do cliente), equipamentos em comodato, atendimento (chamados), serviço de campo
  (ordens de serviço) e financeiro (faturas e pagamentos).
- Três perfis de usuário com responsabilidades diferentes: **ADMIN**, **ATENDENTE** e **TECNICO**.
- Por que esse domínio: é amplo o bastante para 13 entidades com relações reais e tem **regras de negócio** de verdade
  (porta de CTO ocupada, transições de status, comodato de equipamento), que dão sentido aos códigos 409 e aos links HATEOAS.

## 2. Entidades e relacionamentos (≈ 3 min)

**Mostrar:** `docs/der.png` e depois `domain/Contrato.java`, `domain/Cliente.java`, `domain/Tecnico.java`, `domain/OrdemServico.java`.

**Falar:**
- As 13 entidades (Usuario, Cliente, Endereco, Plano, Olt, Cto, Contrato, Equipamento, Tecnico, Chamado, OrdemServico, Fatura, Pagamento).
- O **contrato é o centro**: liga cliente, plano, endereço de instalação e a porta da CTO.
- Os quatro tipos de relacionamento JPA:
  - `@ManyToOne` (ex.: `Contrato.cliente`), sempre com `fetch = LAZY`: o padrão do JPA é EAGER e carregaria dados demais;
  - `@OneToMany(mappedBy)` (ex.: `Cliente.enderecos`, com `cascade` e `orphanRemoval`: o endereço "pertence" ao cliente);
  - `@OneToOne` entre `Tecnico` e `Usuario` (a coluna `usuario_id` é `UNIQUE` no banco);
  - `@ManyToMany` entre `OrdemServico` e `Equipamento`, com a tabela de junção `ordem_servico_equipamento`.
- Decisão: `@Getter/@Setter` do Lombok em vez de `@Data` (o `equals/hashCode/toString` do `@Data` em entidades com
  relacionamentos causa loops e carrega o banco inteiro). `BigDecimal` para dinheiro.

## 3. Organização do projeto e camadas (≈ 3 min)

**Mostrar:** a árvore de pacotes `br.edu.utfpr.netgestor` e os `package-info.java`.

**Falar:** o caminho de uma requisição, usando o cadastro de cliente como exemplo:

```
JSON → ClienteController → ClienteRequest (Bean Validation) → ClienteService (regras, @Transactional)
     → ClienteRepository → PostgreSQL
JSON ← ClienteController ← ClienteAssembler (links HAL) ← ClienteResponse.de(entidade)
```

- `controller/`: só HTTP (rota, verbo, status). Abrir `ClienteController` e mostrar o 201 + `Location`.
- `dto/`: `record`s de entrada e saída; **entidades nunca saem pelo controller** (a senha, por exemplo, nunca aparece).
  Mostrar o construtor compacto do `ClienteRequest` (normaliza o CPF) e a validação `@DocumentoValido`.
- `service/`: regras de negócio. Mostrar no `ContratoService` a regra da porta da CTO e a geração do número.
  Explicar o *dirty checking* (no `atualizar` não precisa chamar `save()`).
- `exception/`: `TratadorDeErros` (`@RestControllerAdvice`) produz o **mesmo JSON de erro** para 400/404/409/500.
- `assembler/`, `security/` e `config/`: explicados nas próximas seções.

## 4. Spring Data JPA (≈ 2 min)

**Mostrar:** `repository/ContratoRepository.java` e `ClienteRepository.java`.

**Falar:**
- Interfaces `JpaRepository<Entidade, Long>`: o CRUD vem pronto.
- **Métodos derivados**: o Spring gera o SQL pelo nome (`existsByClienteId`, `findByCtoIdAndPortaCtoAndStatusNot`).
- **`@Query` em JPQL** para os filtros opcionais: `(:status IS NULL OR c.status = :status)`, uma consulta só para todas as combinações.
- **`@EntityGraph`** evita o problema N+1 (carrega cliente, plano e CTO no mesmo SELECT da listagem).
- `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate **não cria tabelas**, só confere se as entidades batem com o banco.

## 5. Flyway (≈ 2 min)

**Mostrar:** `db/migration/V1`, `V2`, `V3` e o log da subida ("Migrating schema to version 3").
Opcional: `docker exec -it netgestor-db psql -U netgestor -c "select version, description, installed_on from flyway_schema_history"`.

**Falar:**
- **V1** cria a estrutura: PKs, FKs, UNIQUE, CHECKs dos enums e o **índice único parcial** que impede dois contratos
  na mesma porta da CTO (contrato cancelado libera a porta).
- **V2** insere os dados de teste. As senhas são **hashes BCrypt gerados com o próprio `BCryptPasswordEncoder`**.
- **V3** é a evolução real: o **CNPJ alfanumérico** da Receita Federal (desde julho/2026). Não se altera a V1: cria-se uma
  nova migration, e o Flyway aplica em cada banco só o que falta (a tabela `flyway_schema_history` registra o que já rodou).

## 6. Segurança com JWT (≈ 3 min)

**Mostrar:** `security/SecurityConfig`, `JwtService`, `JwtAuthFilter`, `AutenticacaoEntryPoint`, e um `@PreAuthorize` no `ContratoController`.

**Falar:**
- Login (`POST /api/auth/login`): o `AuthenticationManager` confere a senha com o hash BCrypt e o `JwtService` gera o token.
- O token tem `header.payload.assinatura`. O payload pode ser **lido** por qualquer um (Base64), mas não pode ser
  **alterado**: sem a chave secreta, a assinatura deixa de conferir. Por isso não há senha no token.
- `JwtAuthFilter` (`OncePerRequestFilter`): lê o `Authorization: Bearer`, valida e coloca o usuário no `SecurityContext`.
  Ele recarrega o usuário do banco: quem for desativado perde o acesso na hora.
- Duas camadas: o filtro exige token em `/api/**` (senão **401**); o `@PreAuthorize` define o perfil por operação (senão **403**).
  Os dois respondem no mesmo JSON padrão.
- Regra que depende do dado: o técnico só inicia/conclui **as próprias** OS (verificado no service).
- API **stateless**: sem sessão, CSRF desligado, CORS liberado só para `http://localhost:4200`.

## 7. Modelo de Maturidade de Richardson e HATEOAS (≈ 3 min) — ponto mais importante

**Mostrar:** `assembler/ContratoAssembler` + `domain/enums/StatusContrato` + uma resposta real
(`GET /api/contratos/2` antes e depois de `PATCH /api/contratos/2/suspender`).

**Falar, nível por nível:**
- **Nível 0** seria tudo num único endpoint; a API não está nele.
- **Nível 1 (recursos):** cada coisa tem URI própria: `/api/contratos/2`, sub-recursos `/api/contratos/2/faturas`,
  `/api/clientes/1/enderecos`.
- **Nível 2 (verbos e status HTTP):** GET 200, POST **201 + Location**, PUT 200, PATCH para ações, DELETE **204**,
  400 validação, 404, 409 regra de negócio, 401/403 segurança.
- **Nível 3 (HATEOAS):** respostas em HAL (`application/hal+json`) com `_links`. O principal: **links condicionais**.
  Contrato ATIVO traz `suspender` e `cancelar`; depois de suspender, a resposta traz `reativar` e não traz mais `suspender`.
- Decisão de projeto: a regra de transição fica **em um lugar só** (`StatusContrato.podeSuspender()`), usada pelo
  service (409) e pelo assembler (mostra ou não o link). E o link também depende do **perfil**: o técnico lê o contrato
  mas não recebe links de ação.

## 8. Arquitetura do frontend e integração (≈ 2 min)

**Mostrar:** `frontend/src/app` (core/features/shared), `auth.interceptor.ts`, `hateoas.service.ts`.

**Falar:**
- Angular 22, standalone, signals; um service por recurso; o interceptor envia o token e trata o 401.
- O frontend **não repete as regras de negócio**: os botões de ação são montados a partir dos `_links` da resposta.
- Detalhes ficam para o vídeo 3.

## 9. Decisões técnicas, resumo (≈ 1 min)

- Spring Boot 4.1 / Java 21; Flyway com `ddl-auto=validate`; DTOs em `record`.
- Regras garantidas em **duas camadas**: no service (mensagem clara, 409) e no banco (UNIQUE/CHECK como rede de segurança).
- Erro padronizado `{timestamp, status, erro, mensagem, caminho, campos}`.
- Testes: 24 testes automatizados no backend (JPA, consultas, segurança com MockMvc, validador)
  + coleção Postman com 226 requisições.
