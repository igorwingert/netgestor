-- =====================================================================
-- V3 - Suporte ao CNPJ alfanumérico
--
-- Motivo: desde julho de 2026 a Receita Federal emite CNPJs em que as
-- 12 primeiras posições podem conter letras (A-Z) além de números; os 2
-- dígitos verificadores continuam numéricos. Ex.: 12.ABC.345/01DE-35.
-- Os CNPJs antigos (só números) continuam válidos.
--
-- Esta é uma evolução do banco feita com o sistema já em uso: em vez de
-- alterar a V1 (que já foi aplicada), criamos uma nova migration. O Flyway
-- aplica apenas as versões que ainda não rodaram em cada banco.
-- =====================================================================

-- A coluna já comporta 14 caracteres; muda apenas a regra de formato.
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
