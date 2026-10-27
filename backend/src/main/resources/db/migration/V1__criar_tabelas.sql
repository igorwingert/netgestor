-- =====================================================================
-- V1 - Estrutura inicial do banco do NetGestor
--
-- Convenções:
--   * tabelas no singular e colunas em snake_case (o Spring converte
--     automaticamente dataCadastro -> data_cadastro);
--   * chave primária BIGSERIAL (auto incremento) em todas as tabelas;
--   * enums gravados como texto (VARCHAR) + CHECK com os valores válidos;
--   * nomes das constraints: pk_, fk_, uk_ (unique) e ck_ (check).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuários do sistema (login)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id      BIGSERIAL    NOT NULL,
    nome    VARCHAR(100) NOT NULL,
    email   VARCHAR(150) NOT NULL,
    senha   VARCHAR(100) NOT NULL,              -- hash BCrypt, nunca a senha pura
    perfil  VARCHAR(20)  NOT NULL,
    ativo   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ADMIN', 'ATENDENTE', 'TECNICO'))
);

-- ---------------------------------------------------------------------
-- Clientes e endereços
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id             BIGSERIAL    NOT NULL,
    nome           VARCHAR(150) NOT NULL,
    cpf_cnpj       VARCHAR(14)  NOT NULL,       -- somente dígitos
    tipo_pessoa    VARCHAR(10)  NOT NULL,
    email          VARCHAR(150) NOT NULL,
    telefone       VARCHAR(20)  NOT NULL,
    data_cadastro  DATE         NOT NULL DEFAULT CURRENT_DATE,
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_cliente PRIMARY KEY (id),
    CONSTRAINT uk_cliente_cpf_cnpj UNIQUE (cpf_cnpj),
    CONSTRAINT uk_cliente_email UNIQUE (email),
    CONSTRAINT ck_cliente_tipo_pessoa CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    -- pessoa física tem CPF (11 dígitos); jurídica tem CNPJ (14 dígitos)
    CONSTRAINT ck_cliente_documento CHECK (
        (tipo_pessoa = 'FISICA'   AND cpf_cnpj ~ '^[0-9]{11}$') OR
        (tipo_pessoa = 'JURIDICA' AND cpf_cnpj ~ '^[0-9]{14}$')
    )
);

CREATE TABLE endereco (
    id           BIGSERIAL    NOT NULL,
    cep          VARCHAR(8)   NOT NULL,
    logradouro   VARCHAR(150) NOT NULL,
    numero       VARCHAR(10)  NOT NULL,
    complemento  VARCHAR(60),
    bairro       VARCHAR(80)  NOT NULL,
    cidade       VARCHAR(80)  NOT NULL,
    uf           VARCHAR(2)   NOT NULL,
    cliente_id   BIGINT       NOT NULL,
    CONSTRAINT pk_endereco PRIMARY KEY (id),
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT ck_endereco_cep CHECK (cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_endereco_uf CHECK (uf ~ '^[A-Z]{2}$')
);

-- ---------------------------------------------------------------------
-- Planos de internet
-- ---------------------------------------------------------------------
CREATE TABLE plano (
    id             BIGSERIAL     NOT NULL,
    nome           VARCHAR(80)   NOT NULL,
    download_mbps  INTEGER       NOT NULL,
    upload_mbps    INTEGER       NOT NULL,
    valor_mensal   NUMERIC(10,2) NOT NULL,
    tecnologia     VARCHAR(10)   NOT NULL,
    ativo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_plano PRIMARY KEY (id),
    CONSTRAINT uk_plano_nome UNIQUE (nome),
    CONSTRAINT ck_plano_download CHECK (download_mbps > 0),
    CONSTRAINT ck_plano_upload CHECK (upload_mbps > 0),
    CONSTRAINT ck_plano_valor CHECK (valor_mensal > 0),
    CONSTRAINT ck_plano_tecnologia CHECK (tecnologia IN ('FIBRA', 'RADIO'))
);

-- ---------------------------------------------------------------------
-- Rede óptica: OLT (central) -> CTO (caixa na rua) -> cliente
-- ---------------------------------------------------------------------
CREATE TABLE olt (
    id                BIGSERIAL    NOT NULL,
    nome              VARCHAR(60)  NOT NULL,
    ip_gerencia       VARCHAR(45)  NOT NULL,   -- 45 caracteres comportam IPv6
    fabricante        VARCHAR(60)  NOT NULL,
    localizacao       VARCHAR(150),
    total_portas_pon  INTEGER      NOT NULL,
    CONSTRAINT pk_olt PRIMARY KEY (id),
    CONSTRAINT uk_olt_ip_gerencia UNIQUE (ip_gerencia),
    CONSTRAINT ck_olt_total_portas CHECK (total_portas_pon > 0)
);

CREATE TABLE cto (
    id              BIGSERIAL    NOT NULL,
    identificacao   VARCHAR(30)  NOT NULL,
    olt_id          BIGINT       NOT NULL,
    porta_pon       INTEGER      NOT NULL,     -- porta da OLT que alimenta a CTO
    capacidade      INTEGER      NOT NULL,     -- quantidade de portas de cliente
    latitude        NUMERIC(9,6) NOT NULL,
    longitude       NUMERIC(9,6) NOT NULL,
    CONSTRAINT pk_cto PRIMARY KEY (id),
    CONSTRAINT uk_cto_identificacao UNIQUE (identificacao),
    CONSTRAINT fk_cto_olt FOREIGN KEY (olt_id) REFERENCES olt (id),
    CONSTRAINT ck_cto_porta_pon CHECK (porta_pon > 0),
    CONSTRAINT ck_cto_capacidade CHECK (capacidade > 0),
    CONSTRAINT ck_cto_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_cto_longitude CHECK (longitude BETWEEN -180 AND 180)
);

-- ---------------------------------------------------------------------
-- Contratos (cliente + plano + endereço de instalação + porta na CTO)
-- ---------------------------------------------------------------------
CREATE TABLE contrato (
    id                      BIGSERIAL   NOT NULL,
    numero                  VARCHAR(20) NOT NULL,
    cliente_id              BIGINT      NOT NULL,
    plano_id                BIGINT      NOT NULL,
    endereco_instalacao_id  BIGINT      NOT NULL,
    cto_id                  BIGINT,                -- opcional (ex.: plano via rádio)
    porta_cto               INTEGER,
    data_inicio             DATE        NOT NULL,
    dia_vencimento          INTEGER     NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT pk_contrato PRIMARY KEY (id),
    CONSTRAINT uk_contrato_numero UNIQUE (numero),
    CONSTRAINT fk_contrato_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_contrato_plano FOREIGN KEY (plano_id) REFERENCES plano (id),
    CONSTRAINT fk_contrato_endereco FOREIGN KEY (endereco_instalacao_id) REFERENCES endereco (id),
    CONSTRAINT fk_contrato_cto FOREIGN KEY (cto_id) REFERENCES cto (id),
    CONSTRAINT ck_contrato_dia_vencimento CHECK (dia_vencimento BETWEEN 1 AND 28),
    CONSTRAINT ck_contrato_status CHECK (status IN ('ATIVO', 'SUSPENSO', 'CANCELADO')),
    -- CTO e porta andam juntas: ou as duas são informadas, ou nenhuma
    CONSTRAINT ck_contrato_cto_porta CHECK (
        (cto_id IS NULL AND porta_cto IS NULL) OR
        (cto_id IS NOT NULL AND porta_cto > 0)
    )
);

-- Uma porta da CTO só pode ser usada por um contrato que não esteja cancelado.
-- (índice único parcial: contratos cancelados liberam a porta)
CREATE UNIQUE INDEX uk_contrato_cto_porta_em_uso
    ON contrato (cto_id, porta_cto)
    WHERE status <> 'CANCELADO';

-- ---------------------------------------------------------------------
-- Equipamentos (ONU / roteador), em estoque ou em comodato com um contrato
-- ---------------------------------------------------------------------
CREATE TABLE equipamento (
    id             BIGSERIAL   NOT NULL,
    tipo           VARCHAR(10) NOT NULL,
    fabricante     VARCHAR(60) NOT NULL,
    modelo         VARCHAR(60) NOT NULL,
    numero_serie   VARCHAR(40) NOT NULL,
    mac            VARCHAR(17) NOT NULL,           -- formato AA:BB:CC:DD:EE:FF
    status         VARCHAR(10) NOT NULL DEFAULT 'ESTOQUE',
    contrato_id    BIGINT,
    CONSTRAINT pk_equipamento PRIMARY KEY (id),
    CONSTRAINT uk_equipamento_numero_serie UNIQUE (numero_serie),
    CONSTRAINT uk_equipamento_mac UNIQUE (mac),
    CONSTRAINT fk_equipamento_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    CONSTRAINT ck_equipamento_tipo CHECK (tipo IN ('ONU', 'ROTEADOR')),
    CONSTRAINT ck_equipamento_status CHECK (status IN ('ESTOQUE', 'INSTALADO', 'DEFEITO')),
    CONSTRAINT ck_equipamento_mac CHECK (mac ~ '^([0-9A-F]{2}:){5}[0-9A-F]{2}$'),
    -- está INSTALADO se, e somente se, estiver vinculado a um contrato
    CONSTRAINT ck_equipamento_vinculo CHECK ((status = 'INSTALADO') = (contrato_id IS NOT NULL))
);

-- ---------------------------------------------------------------------
-- Técnicos (cada técnico tem exatamente um usuário de login: 1:1)
-- ---------------------------------------------------------------------
CREATE TABLE tecnico (
    id             BIGSERIAL    NOT NULL,
    nome           VARCHAR(100) NOT NULL,
    telefone       VARCHAR(20)  NOT NULL,
    especialidade  VARCHAR(60)  NOT NULL,
    usuario_id     BIGINT       NOT NULL,
    CONSTRAINT pk_tecnico PRIMARY KEY (id),
    CONSTRAINT uk_tecnico_usuario UNIQUE (usuario_id),   -- garante o 1:1
    CONSTRAINT fk_tecnico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- ---------------------------------------------------------------------
-- Chamados (atendimento ao cliente)
-- ---------------------------------------------------------------------
CREATE TABLE chamado (
    id               BIGSERIAL     NOT NULL,
    protocolo        VARCHAR(20)   NOT NULL,
    contrato_id      BIGINT        NOT NULL,
    aberto_por_id    BIGINT        NOT NULL,
    tipo             VARCHAR(20)   NOT NULL,
    prioridade       VARCHAR(10)   NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'ABERTO',
    descricao        VARCHAR(1000) NOT NULL,
    data_abertura    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento  TIMESTAMP,
    CONSTRAINT pk_chamado PRIMARY KEY (id),
    CONSTRAINT uk_chamado_protocolo UNIQUE (protocolo),
    CONSTRAINT fk_chamado_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    CONSTRAINT fk_chamado_usuario FOREIGN KEY (aberto_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_chamado_tipo CHECK (tipo IN ('SEM_CONEXAO', 'LENTIDAO', 'FINANCEIRO', 'OUTRO')),
    CONSTRAINT ck_chamado_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT ck_chamado_status CHECK (status IN ('ABERTO', 'EM_ATENDIMENTO', 'RESOLVIDO', 'FECHADO')),
    -- chamado FECHADO precisa da data de fechamento, e ela não pode ser antes da abertura
    CONSTRAINT ck_chamado_fechamento CHECK (status <> 'FECHADO' OR data_fechamento IS NOT NULL),
    CONSTRAINT ck_chamado_datas CHECK (data_fechamento IS NULL OR data_fechamento >= data_abertura)
);

-- ---------------------------------------------------------------------
-- Ordens de serviço (trabalho de campo do técnico)
-- ---------------------------------------------------------------------
CREATE TABLE ordem_servico (
    id              BIGSERIAL     NOT NULL,
    numero          VARCHAR(20)   NOT NULL,
    contrato_id     BIGINT        NOT NULL,
    chamado_id      BIGINT,                         -- opcional: OS de instalação não nasce de chamado
    tecnico_id      BIGINT        NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'AGENDADA',
    data_agendada   TIMESTAMP     NOT NULL,
    data_conclusao  TIMESTAMP,
    observacoes     VARCHAR(1000),
    CONSTRAINT pk_ordem_servico PRIMARY KEY (id),
    CONSTRAINT uk_ordem_servico_numero UNIQUE (numero),
    CONSTRAINT fk_ordem_servico_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    CONSTRAINT fk_ordem_servico_chamado FOREIGN KEY (chamado_id) REFERENCES chamado (id),
    CONSTRAINT fk_ordem_servico_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnico (id),
    CONSTRAINT ck_ordem_servico_tipo CHECK (tipo IN ('INSTALACAO', 'REPARO', 'RETIRADA', 'MUDANCA_ENDERECO')),
    CONSTRAINT ck_ordem_servico_status CHECK (status IN ('AGENDADA', 'EM_EXECUCAO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT ck_ordem_servico_conclusao CHECK (status <> 'CONCLUIDA' OR data_conclusao IS NOT NULL)
);

-- Tabela de junção do relacionamento N:N entre ordem de serviço e equipamento
CREATE TABLE ordem_servico_equipamento (
    ordem_servico_id  BIGINT NOT NULL,
    equipamento_id    BIGINT NOT NULL,
    CONSTRAINT pk_ordem_servico_equipamento PRIMARY KEY (ordem_servico_id, equipamento_id),
    CONSTRAINT fk_ose_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id) ON DELETE CASCADE,
    CONSTRAINT fk_ose_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamento (id)
);

-- ---------------------------------------------------------------------
-- Financeiro: faturas mensais e seus pagamentos
-- ---------------------------------------------------------------------
CREATE TABLE fatura (
    id               BIGSERIAL     NOT NULL,
    contrato_id      BIGINT        NOT NULL,
    competencia      DATE          NOT NULL,       -- sempre o 1º dia do mês de referência
    valor            NUMERIC(10,2) NOT NULL,
    data_vencimento  DATE          NOT NULL,
    status           VARCHAR(10)   NOT NULL DEFAULT 'ABERTA',
    CONSTRAINT pk_fatura PRIMARY KEY (id),
    CONSTRAINT fk_fatura_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    -- um contrato tem no máximo uma fatura por mês
    CONSTRAINT uk_fatura_contrato_competencia UNIQUE (contrato_id, competencia),
    CONSTRAINT ck_fatura_competencia CHECK (EXTRACT(DAY FROM competencia) = 1),
    CONSTRAINT ck_fatura_valor CHECK (valor > 0),
    CONSTRAINT ck_fatura_status CHECK (status IN ('ABERTA', 'PAGA', 'VENCIDA', 'CANCELADA'))
);

CREATE TABLE pagamento (
    id              BIGSERIAL     NOT NULL,
    fatura_id       BIGINT        NOT NULL,
    data_pagamento  DATE          NOT NULL,
    valor_pago      NUMERIC(10,2) NOT NULL,
    forma           VARCHAR(10)   NOT NULL,
    CONSTRAINT pk_pagamento PRIMARY KEY (id),
    CONSTRAINT fk_pagamento_fatura FOREIGN KEY (fatura_id) REFERENCES fatura (id),
    CONSTRAINT ck_pagamento_valor CHECK (valor_pago > 0),
    CONSTRAINT ck_pagamento_forma CHECK (forma IN ('PIX', 'BOLETO', 'CARTAO'))
);

-- ---------------------------------------------------------------------
-- Índices nas chaves estrangeiras mais consultadas.
-- O PostgreSQL cria índice automático para PK e UNIQUE, mas NÃO para FK.
-- ---------------------------------------------------------------------
CREATE INDEX ix_endereco_cliente      ON endereco (cliente_id);
CREATE INDEX ix_contrato_cliente      ON contrato (cliente_id);
CREATE INDEX ix_contrato_status       ON contrato (status);
CREATE INDEX ix_equipamento_contrato  ON equipamento (contrato_id);
CREATE INDEX ix_chamado_contrato      ON chamado (contrato_id);
CREATE INDEX ix_chamado_status        ON chamado (status);
CREATE INDEX ix_ordem_servico_tecnico ON ordem_servico (tecnico_id);
CREATE INDEX ix_fatura_contrato       ON fatura (contrato_id);
CREATE INDEX ix_fatura_status         ON fatura (status);
CREATE INDEX ix_pagamento_fatura      ON pagamento (fatura_id);
