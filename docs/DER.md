# DER - Diagrama Entidade-Relacionamento

Modelo físico do banco do NetGestor (PostgreSQL 16), criado e evoluído pelas migrations do Flyway
(`backend/src/main/resources/db/migration`). São **13 entidades** e uma tabela de junção para o relacionamento N:N.

![DER do NetGestor](der.png)

<details>
<summary>Código Mermaid do diagrama (renderizado automaticamente no GitHub)</summary>

```mermaid
erDiagram
    USUARIO {
        bigint id PK
        varchar nome
        varchar email UK
        varchar senha "hash BCrypt"
        varchar perfil "ADMIN, ATENDENTE, TECNICO"
        boolean ativo
    }
    CLIENTE {
        bigint id PK
        varchar nome
        varchar cpf_cnpj UK "CPF ou CNPJ (alfanumérico desde a V3)"
        varchar tipo_pessoa "FISICA, JURIDICA"
        varchar email UK
        varchar telefone
        date data_cadastro
        boolean ativo
    }
    ENDERECO {
        bigint id PK
        varchar cep
        varchar logradouro
        varchar numero
        varchar complemento
        varchar bairro
        varchar cidade
        varchar uf
        bigint cliente_id FK
    }
    PLANO {
        bigint id PK
        varchar nome UK
        integer download_mbps
        integer upload_mbps
        numeric valor_mensal
        varchar tecnologia "FIBRA, RADIO"
        boolean ativo
    }
    OLT {
        bigint id PK
        varchar nome
        varchar ip_gerencia UK
        varchar fabricante
        varchar localizacao
        integer total_portas_pon
    }
    CTO {
        bigint id PK
        varchar identificacao UK
        bigint olt_id FK
        integer porta_pon
        integer capacidade
        numeric latitude
        numeric longitude
    }
    CONTRATO {
        bigint id PK
        varchar numero UK
        bigint cliente_id FK
        bigint plano_id FK
        bigint endereco_instalacao_id FK
        bigint cto_id FK "opcional"
        integer porta_cto
        date data_inicio
        integer dia_vencimento
        varchar status "ATIVO, SUSPENSO, CANCELADO"
    }
    EQUIPAMENTO {
        bigint id PK
        varchar tipo "ONU, ROTEADOR"
        varchar fabricante
        varchar modelo
        varchar numero_serie UK
        varchar mac UK
        varchar status "ESTOQUE, INSTALADO, DEFEITO"
        bigint contrato_id FK "opcional (comodato)"
    }
    TECNICO {
        bigint id PK
        varchar nome
        varchar telefone
        varchar especialidade
        bigint usuario_id FK, UK "1:1 com usuario"
    }
    CHAMADO {
        bigint id PK
        varchar protocolo UK
        bigint contrato_id FK
        bigint aberto_por_id FK
        varchar tipo
        varchar prioridade
        varchar status "ABERTO, EM_ATENDIMENTO, RESOLVIDO, FECHADO"
        varchar descricao
        timestamp data_abertura
        timestamp data_fechamento
    }
    ORDEM_SERVICO {
        bigint id PK
        varchar numero UK
        bigint contrato_id FK
        bigint chamado_id FK "opcional"
        bigint tecnico_id FK
        varchar tipo
        varchar status "AGENDADA, EM_EXECUCAO, CONCLUIDA, CANCELADA"
        timestamp data_agendada
        timestamp data_conclusao
        varchar observacoes
    }
    ORDEM_SERVICO_EQUIPAMENTO {
        bigint ordem_servico_id PK, FK
        bigint equipamento_id PK, FK
    }
    FATURA {
        bigint id PK
        bigint contrato_id FK
        date competencia "1º dia do mês"
        numeric valor
        date data_vencimento
        varchar status "ABERTA, PAGA, VENCIDA, CANCELADA"
    }
    PAGAMENTO {
        bigint id PK
        bigint fatura_id FK
        date data_pagamento
        numeric valor_pago
        varchar forma "PIX, BOLETO, CARTAO"
    }

    CLIENTE ||--o{ ENDERECO : possui
    CLIENTE ||--o{ CONTRATO : assina
    PLANO ||--o{ CONTRATO : define
    ENDERECO ||--o{ CONTRATO : "local de instalação"
    OLT ||--o{ CTO : alimenta
    CTO |o--o{ CONTRATO : "porta usada por"
    CONTRATO |o--o{ EQUIPAMENTO : "comodato"
    CONTRATO ||--o{ CHAMADO : gera
    CONTRATO ||--o{ ORDEM_SERVICO : tem
    CONTRATO ||--o{ FATURA : cobra
    FATURA ||--o{ PAGAMENTO : recebe
    CHAMADO |o--o{ ORDEM_SERVICO : origina
    TECNICO ||--o{ ORDEM_SERVICO : executa
    USUARIO ||--o| TECNICO : "login de"
    USUARIO ||--o{ CHAMADO : abre
    ORDEM_SERVICO ||--o{ ORDEM_SERVICO_EQUIPAMENTO : utiliza
    EQUIPAMENTO ||--o{ ORDEM_SERVICO_EQUIPAMENTO : "usado em"
```

</details>

## Relacionamentos e mapeamento JPA

| Relacionamento | Tipo | Mapeamento JPA |
|---|---|---|
| Cliente → Endereços | 1:N | `@OneToMany(mappedBy = "cliente", cascade = ALL, orphanRemoval = true)` em `Cliente`; `@ManyToOne` em `Endereco` |
| Cliente / Plano / Endereço / CTO → Contrato | 1:N | `@ManyToOne(fetch = LAZY)` em `Contrato` (CTO opcional) |
| OLT → CTOs | 1:N | `@OneToMany(mappedBy = "olt")` em `Olt`; `@ManyToOne` em `Cto` |
| Contrato → Equipamentos, Chamados, OS, Faturas | 1:N | `@ManyToOne` no lado filho |
| Fatura → Pagamentos | 1:N | `@OneToMany(mappedBy = "fatura")` em `Fatura`; `@ManyToOne` em `Pagamento` |
| Chamado → OS / Técnico → OS / Usuário → Chamado | 1:N | `@ManyToOne` no lado filho (chamado da OS é opcional) |
| **Técnico ↔ Usuário** | **1:1** | `@OneToOne` + `usuario_id UNIQUE` no banco |
| **Ordem de serviço ↔ Equipamento** | **N:N** | `@ManyToMany` + `@JoinTable(name = "ordem_servico_equipamento")` |

## Restrições importantes (garantidas pelo próprio banco)

- `UNIQUE`: e-mail de usuário e de cliente, CPF/CNPJ, nome do plano, IP da OLT, identificação da CTO,
  número do contrato, número de série e MAC do equipamento, protocolo do chamado, número da OS,
  fatura por contrato + competência (`uk_fatura_contrato_competencia`).
- **Índice único parcial** `uk_contrato_cto_porta_em_uso (cto_id, porta_cto) WHERE status <> 'CANCELADO'`:
  uma porta da CTO só pode ter um contrato que não esteja cancelado.
- `CHECK` dos enums (valores permitidos) e de valores positivos (velocidades, preços, capacidade).
- `CHECK` de consistência: pessoa física → CPF de 11 dígitos, jurídica → CNPJ; CTO e porta informadas juntas;
  equipamento `INSTALADO` se e somente se tiver contrato; chamado `FECHADO` exige `data_fechamento`;
  OS `CONCLUIDA` exige `data_conclusao`; competência sempre no 1º dia do mês.

## Migrations (versionamento do banco)

| Versão | Arquivo | O que faz |
|---|---|---|
| V1 | `V1__criar_tabelas.sql` | Cria as 14 tabelas, PKs, FKs, UNIQUEs, CHECKs e índices |
| V2 | `V2__dados_iniciais.sql` | Dados de teste: usuários dos 3 perfis (senhas com BCrypt), planos, rede, clientes, contratos, equipamentos, chamados, OS, faturas e pagamentos |
| V3 | `V3__cnpj_alfanumerico.sql` | Evolução: aceita o CNPJ alfanumérico da Receita Federal (12 caracteres 0-9/A-Z + 2 dígitos verificadores) e cadastra um cliente de exemplo |
