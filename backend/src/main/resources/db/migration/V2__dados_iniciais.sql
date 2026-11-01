-- =====================================================================
-- V2 - dados iniciais para testes
--
-- como o Flyway roda esta migration logo apos a V1 (banco vazio), os ids
-- gerados pelo BIGSERIAL sao previsiveis: 1, 2, 3... os comentarios
-- "-- id N" ao lado de cada linha ajudam a ler as chaves estrangeiras.
--
-- CPFs e CNPJs sao ficticios, mas com digitos verificadores validos.
-- =====================================================================

-- ---------------------------------------------------------------------
-- usuarios. hashes BCrypt gerados com BCryptPasswordEncoder (Spring Security):
--   admin123 / atendente123 / tecnico123
-- ---------------------------------------------------------------------
INSERT INTO usuario (nome, email, senha, perfil, ativo) VALUES
    ('Administrador',  'admin@netgestor.com',     '$2a$10$psm.yhE.jVT2yj13ziK7YeNhoPlBkXJZ2Usw0N4azQ4NMKqHppE0.', 'ADMIN',     TRUE),  -- id 1
    ('Juliana Ramos',  'atendente@netgestor.com', '$2a$10$8CgCCQ6zo3U8I9Yt/Ge5neYpOAA9VZ9l1D3NhX.SPsi2eAdSLRyLy', 'ATENDENTE', TRUE),  -- id 2
    ('Carlos Souza',   'tecnico@netgestor.com',   '$2a$10$T26q9jDERe5WMtYfDjTo1e7HajvaU.x6Esf4MbU7tHT0qQuJzakoi', 'TECNICO',   TRUE),  -- id 3
    ('Marina Lopes',   'tecnico2@netgestor.com',  '$2a$10$T26q9jDERe5WMtYfDjTo1e7HajvaU.x6Esf4MbU7tHT0qQuJzakoi', 'TECNICO',   TRUE);  -- id 4 (senha tecnico123)

-- ---------------------------------------------------------------------
-- planos
-- ---------------------------------------------------------------------
INSERT INTO plano (nome, download_mbps, upload_mbps, valor_mensal, tecnologia, ativo) VALUES
    ('Fibra 300 Mega',  300,  150,  99.90, 'FIBRA', TRUE),   -- id 1
    ('Fibra 500 Mega',  500,  250, 129.90, 'FIBRA', TRUE),   -- id 2
    ('Fibra 1 Giga',   1000,  500, 199.90, 'FIBRA', TRUE),   -- id 3
    ('Rádio 20 Mega',    20,    5,  79.90, 'RADIO', TRUE),   -- id 4
    ('Fibra 100 Mega',  100,   50,  79.90, 'FIBRA', FALSE);  -- id 5 (plano antigo, fora de venda)

-- ---------------------------------------------------------------------
-- rede: OLTs e CTOs
-- ---------------------------------------------------------------------
INSERT INTO olt (nome, ip_gerencia, fabricante, localizacao, total_portas_pon) VALUES
    ('OLT-CENTRO', '10.10.0.1', 'Huawei',    'POP Centro - Rua XV de Novembro, Curitiba/PR', 16),  -- id 1
    ('OLT-NORTE',  '10.10.0.2', 'ZTE',       'POP Norte - Av. Paraná, Curitiba/PR',          16),  -- id 2
    ('OLT-SUL',    '10.10.0.3', 'Fiberhome', 'POP Sul - em implantação',                      8);  -- id 3 (sem CTOs: pode ser excluida)

INSERT INTO cto (identificacao, olt_id, porta_pon, capacidade, latitude, longitude) VALUES
    ('CTO-CEN-01', 1, 1,  8, -25.429596, -49.271272),  -- id 1
    ('CTO-CEN-02', 1, 2, 16, -25.436200, -49.265800),  -- id 2
    ('CTO-NOR-01', 2, 1,  8, -25.410300, -49.258900),  -- id 3
    ('CTO-NOR-02', 2, 3, 16, -25.398700, -49.251400);  -- id 4 (sem clientes ainda)

-- ---------------------------------------------------------------------
-- clientes e enderecos
-- ---------------------------------------------------------------------
INSERT INTO cliente (nome, cpf_cnpj, tipo_pessoa, email, telefone, data_cadastro, ativo) VALUES
    ('João da Silva',           '12345678909',    'FISICA',   'joao.silva@email.com',       '(41) 99911-1111', '2025-03-10', TRUE),  -- id 1
    ('Maria Oliveira',          '98765432100',    'FISICA',   'maria.oliveira@email.com',   '(41) 99922-2222', '2024-01-25', TRUE),  -- id 2
    ('Pedro Santos',            '45612378955',    'FISICA',   'pedro.santos@email.com',     '(41) 99933-3333', '2026-01-15', TRUE),  -- id 3
    ('Ana Costa',               '32165498791',    'FISICA',   'ana.costa@email.com',        '(41) 99944-4444', '2026-09-05', TRUE),  -- id 4
    ('Padaria Pão Quente Ltda', '11222333000181', 'JURIDICA', 'contato@paoquente.com.br',   '(41) 3333-5555',  '2025-11-02', TRUE),  -- id 5
    ('Tech Solutions ME',       '44555666000181', 'JURIDICA', 'financeiro@techsol.com.br',  '(41) 3333-6666',  '2026-10-20', TRUE);  -- id 6 (sem contratos: pode ser excluido)

INSERT INTO endereco (cep, logradouro, numero, complemento, bairro, cidade, uf, cliente_id) VALUES
    ('80020310', 'Rua XV de Novembro',      '1000', 'Apto 302',  'Centro',        'Curitiba', 'PR', 1),  -- id 1
    ('80410200', 'Rua das Flores',          '25',   NULL,        'Centro',        'Curitiba', 'PR', 1),  -- id 2
    ('80060070', 'Avenida Sete de Setembro','2500', 'Casa 2',    'Rebouças',      'Curitiba', 'PR', 2),  -- id 3
    ('80010010', 'Rua Marechal Deodoro',    '450',  NULL,        'Centro',        'Curitiba', 'PR', 3),  -- id 4
    ('80730000', 'Rua Padre Anchieta',      '1200', NULL,        'Bigorrilho',    'Curitiba', 'PR', 4),  -- id 5
    ('80030000', 'Rua João Gualberto',      '800',  'Loja 1',    'Alto da Glória','Curitiba', 'PR', 5),  -- id 6
    ('82510000', 'Avenida Paraná',          '3000', 'Sala 10',   'Cabral',        'Curitiba', 'PR', 6);  -- id 7

-- ---------------------------------------------------------------------
-- contratos
-- ---------------------------------------------------------------------
INSERT INTO contrato (numero, cliente_id, plano_id, endereco_instalacao_id, cto_id, porta_cto, data_inicio, dia_vencimento, status) VALUES
    ('CT-2025-0001', 1, 2, 1, 1,    1,    '2025-03-12', 10, 'ATIVO'),      -- id 1
    ('CT-2025-0002', 2, 1, 3, 1,    2,    '2025-06-25', 15, 'ATIVO'),      -- id 2
    ('CT-2025-0003', 5, 3, 6, 2,    1,    '2025-11-05',  5, 'ATIVO'),      -- id 3
    ('CT-2026-0001', 3, 1, 4, 3,    1,    '2026-01-20', 10, 'SUSPENSO'),   -- id 4 (inadimplente)
    ('CT-2026-0002', 4, 4, 5, NULL, NULL, '2026-09-08', 20, 'ATIVO'),      -- id 5 (radio: sem CTO)
    -- contrato antigo cancelado: usava a mesma porta 2 da CTO-CEN-01 que hoje
    -- pertence ao contrato 2 (permitido pelo indice unico parcial da V1)
    ('CT-2024-0001', 2, 5, 3, 1,    2,    '2024-02-01', 15, 'CANCELADO');  -- id 6

-- ---------------------------------------------------------------------
-- equipamentos (INSTALADO = em comodato com um contrato)
-- ---------------------------------------------------------------------
INSERT INTO equipamento (tipo, fabricante, modelo, numero_serie, mac, status, contrato_id) VALUES
    ('ONU',      'Huawei',    'EG8145V5',     'HWTC1A2B3C01', '48:57:02:1A:2B:01', 'INSTALADO', 1),     -- id 1
    ('ROTEADOR', 'TP-Link',   'Archer C6',    'TPL22110001',  '50:C7:BF:11:00:01', 'INSTALADO', 1),     -- id 2
    ('ONU',      'Huawei',    'EG8145V5',     'HWTC1A2B3C02', '48:57:02:1A:2B:02', 'INSTALADO', 2),     -- id 3
    ('ONU',      'ZTE',       'F670L',        'ZTEG00000301', '34:24:3E:00:03:01', 'INSTALADO', 3),     -- id 4
    ('ROTEADOR', 'TP-Link',   'Archer AX23',  'TPL22110002',  '50:C7:BF:11:00:02', 'INSTALADO', 3),     -- id 5
    ('ONU',      'ZTE',       'F670L',        'ZTEG00000302', '34:24:3E:00:03:02', 'INSTALADO', 4),     -- id 6
    ('ROTEADOR', 'Intelbras', 'W5-1200F',     'ITB55000001',  '00:1A:3F:55:00:01', 'INSTALADO', 5),     -- id 7
    ('ONU',      'Huawei',    'EG8145V5',     'HWTC1A2B3C03', '48:57:02:1A:2B:03', 'ESTOQUE',   NULL),  -- id 8
    ('ONU',      'ZTE',       'F670L',        'ZTEG00000303', '34:24:3E:00:03:03', 'ESTOQUE',   NULL),  -- id 9
    ('ROTEADOR', 'TP-Link',   'Archer C6',    'TPL22110003',  '50:C7:BF:11:00:03', 'ESTOQUE',   NULL),  -- id 10
    ('ROTEADOR', 'TP-Link',   'Archer C6',    'TPL22110004',  '50:C7:BF:11:00:04', 'DEFEITO',   NULL),  -- id 11
    ('ONU',      'Huawei',    'EG8145V5',     'HWTC1A2B3C04', '48:57:02:1A:2B:04', 'ESTOQUE',   NULL);  -- id 12

-- ---------------------------------------------------------------------
-- tecnicos (vinculados aos usuarios de perfil TECNICO)
-- ---------------------------------------------------------------------
INSERT INTO tecnico (nome, telefone, especialidade, usuario_id) VALUES
    ('Carlos Souza', '(41) 99800-1001', 'Fibra óptica',    3),  -- id 1
    ('Marina Lopes', '(41) 99800-1002', 'Redes e rádio',   4);  -- id 2

-- ---------------------------------------------------------------------
-- chamados
-- ---------------------------------------------------------------------
INSERT INTO chamado (protocolo, contrato_id, aberto_por_id, tipo, prioridade, status, descricao, data_abertura, data_fechamento) VALUES
    ('CH-2026-000001', 1, 2, 'LENTIDAO',    'MEDIA',   'FECHADO',
        'Cliente relata lentidão no período noturno.',                         '2026-09-02 09:15', '2026-09-03 17:40'),  -- id 1
    ('CH-2026-000002', 3, 2, 'SEM_CONEXAO', 'CRITICA', 'EM_ATENDIMENTO',
        'Empresa sem conexão desde a manhã; LED LOS vermelho na ONU.',         '2026-10-24 08:05', NULL),                -- id 2
    ('CH-2026-000003', 2, 2, 'FINANCEIRO',  'BAIXA',   'ABERTO',
        'Cliente solicita segunda via da fatura de outubro.',                  '2026-10-25 14:30', NULL),                -- id 3
    ('CH-2026-000004', 4, 2, 'FINANCEIRO',  'MEDIA',   'RESOLVIDO',
        'Contrato suspenso por inadimplência; cliente pediu negociação.',      '2026-10-10 10:00', NULL),                -- id 4
    ('CH-2026-000005', 5, 1, 'SEM_CONEXAO', 'ALTA',    'ABERTO',
        'Sinal do rádio instável após chuva forte.',                           '2026-10-26 07:50', NULL);                -- id 5

-- ---------------------------------------------------------------------
-- ordens de servico e equipamentos utilizados (N:N)
-- ---------------------------------------------------------------------
INSERT INTO ordem_servico (numero, contrato_id, chamado_id, tecnico_id, tipo, status, data_agendada, data_conclusao, observacoes) VALUES
    ('OS-2026-0001', 1, 1,    1, 'REPARO',           'CONCLUIDA',   '2026-09-03 14:00', '2026-09-03 16:30',
        'Conector óptico substituído; sinal normalizado em -19 dBm.'),        -- id 1
    ('OS-2026-0002', 5, NULL, 2, 'INSTALACAO',       'CONCLUIDA',   '2026-09-08 09:00', '2026-09-08 11:20',
        'Instalação via rádio concluída; antena alinhada com a torre norte.'),  -- id 2
    ('OS-2026-0003', 3, 2,    1, 'REPARO',           'EM_EXECUCAO', '2026-10-24 13:00', NULL,
        'Verificar fibra drop entre a CTO e o cliente.'),                     -- id 3
    ('OS-2026-0004', 5, 5,    2, 'REPARO',           'AGENDADA',    '2026-10-27 09:00', NULL,
        NULL),                                                                -- id 4
    ('OS-2026-0005', 1, NULL, 1, 'MUDANCA_ENDERECO', 'AGENDADA',    '2026-10-29 14:00', NULL,
        'Mudança para o endereço da Rua das Flores, 25.');                    -- id 5

INSERT INTO ordem_servico_equipamento (ordem_servico_id, equipamento_id) VALUES
    (1, 1),   -- OS 1 trabalhou na ONU do contrato 1
    (2, 7),   -- OS 2 instalou o roteador do contrato 5
    (3, 4);   -- OS 3 esta verificando a ONU do contrato 3

-- ---------------------------------------------------------------------
-- faturas (valor = mensalidade do plano; vencimento = dia do contrato)
-- ---------------------------------------------------------------------
INSERT INTO fatura (contrato_id, competencia, valor, data_vencimento, status) VALUES
    (1, '2026-08-01', 129.90, '2026-08-10', 'PAGA'),       -- id 1
    (1, '2026-09-01', 129.90, '2026-09-10', 'PAGA'),       -- id 2
    (1, '2026-10-01', 129.90, '2026-10-10', 'PAGA'),       -- id 3
    (1, '2026-11-01', 129.90, '2026-11-10', 'ABERTA'),     -- id 4
    (2, '2026-08-01',  99.90, '2026-08-15', 'PAGA'),       -- id 5
    (2, '2026-09-01',  99.90, '2026-09-15', 'PAGA'),       -- id 6
    (2, '2026-10-01',  99.90, '2026-10-15', 'VENCIDA'),    -- id 7
    (3, '2026-08-01', 199.90, '2026-08-05', 'PAGA'),       -- id 8
    (3, '2026-09-01', 199.90, '2026-09-05', 'PAGA'),       -- id 9
    (3, '2026-10-01', 199.90, '2026-10-05', 'PAGA'),       -- id 10
    (3, '2026-11-01', 199.90, '2026-11-05', 'ABERTA'),     -- id 11
    (4, '2026-08-01',  99.90, '2026-08-10', 'PAGA'),       -- id 12
    (4, '2026-09-01',  99.90, '2026-09-10', 'VENCIDA'),    -- id 13
    (4, '2026-10-01',  99.90, '2026-10-10', 'VENCIDA'),    -- id 14
    (5, '2026-09-01',  79.90, '2026-09-20', 'PAGA'),       -- id 15
    (5, '2026-10-01',  79.90, '2026-10-20', 'PAGA'),       -- id 16
    (6, '2024-03-01',  79.90, '2024-03-15', 'CANCELADA');  -- id 17

-- ---------------------------------------------------------------------
-- pagamentos
-- ---------------------------------------------------------------------
INSERT INTO pagamento (fatura_id, data_pagamento, valor_pago, forma) VALUES
    (1,  '2026-08-09', 129.90, 'PIX'),
    (2,  '2026-09-10', 129.90, 'PIX'),
    (3,  '2026-10-08', 129.90, 'CARTAO'),
    (5,  '2026-08-15',  99.90, 'BOLETO'),
    (6,  '2026-09-14',  99.90, 'BOLETO'),
    (8,  '2026-08-05', 199.90, 'BOLETO'),
    (9,  '2026-09-04', 199.90, 'BOLETO'),
    (10, '2026-10-03', 199.90, 'PIX'),
    (12, '2026-08-12',  99.90, 'PIX'),
    (13, '2026-10-12',  50.00, 'PIX'),      -- pagamento parcial: a fatura continua VENCIDA
    (15, '2026-09-19',  79.90, 'CARTAO'),
    (16, '2026-10-20',  79.90, 'CARTAO');
