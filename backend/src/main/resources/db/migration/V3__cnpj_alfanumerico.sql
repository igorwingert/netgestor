-- =====================================================================
-- V3 - suporte ao CNPJ alfanumerico
--
-- motivo: desde julho de 2026 a Receita Federal emite CNPJs em que as
-- 12 primeiras posicoes podem conter letras (A-Z) alem de numeros; os 2
-- digitos verificadores continuam numericos. ex.: 12.ABC.345/01DE-35.
-- os CNPJs antigos (so numeros) continuam validos.
--
-- esta e uma evolucao do banco feita com o sistema ja em uso: em vez de
-- alterar a V1 (que ja foi aplicada), criamos uma nova migration. o Flyway
-- aplica apenas as versoes que ainda nao rodaram em cada banco.
-- =====================================================================

-- a coluna ja comporta 14 caracteres; muda apenas a regra de formato.
ALTER TABLE cliente DROP CONSTRAINT ck_cliente_documento;

ALTER TABLE cliente ADD CONSTRAINT ck_cliente_documento CHECK (
    (tipo_pessoa = 'FISICA'   AND cpf_cnpj ~ '^[0-9]{11}$') OR
    (tipo_pessoa = 'JURIDICA' AND cpf_cnpj ~ '^[0-9A-Z]{12}[0-9]{2}$')
);

COMMENT ON COLUMN cliente.cpf_cnpj IS
    'CPF (11 dígitos) ou CNPJ (14 caracteres: 12 alfanuméricos + 2 dígitos verificadores), sem máscara';

-- Cliente de exemplo com o novo formato (o exemplo oficial da Receita: 12.ABC.345/01DE-35)
INSERT INTO cliente (nome, cpf_cnpj, tipo_pessoa, email, telefone, data_cadastro, ativo) VALUES
    ('Nova Era Tecnologia Ltda', '12ABC34501DE35', 'JURIDICA', 'contato@novaera.com.br', '(41) 3333-7777', '2026-10-27', TRUE);
