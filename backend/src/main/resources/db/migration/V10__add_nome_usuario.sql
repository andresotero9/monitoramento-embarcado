-- Adiciona o nome completo do usuário, exigido pelo cadastro público (self-service).
-- Backfill do usuário admin padrão (criado pela V8, antes desta coluna existir)
-- antes de tornar a coluna obrigatória.
ALTER TABLE usuario ADD COLUMN nome VARCHAR(150);

UPDATE usuario SET nome = 'Administrador' WHERE nome IS NULL;

ALTER TABLE usuario ALTER COLUMN nome SET NOT NULL;

COMMENT ON COLUMN usuario.nome IS 'Nome completo do usuário';
