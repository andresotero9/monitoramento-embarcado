-- Tabela de usuários do sistema para autenticação.
-- A senha é sempre armazenada como hash BCrypt, nunca em texto puro.
CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao    TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_atualizacao TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_role CHECK (role IN ('ADMIN', 'OPERADOR'))
);

COMMENT ON TABLE usuario IS 'Usuários com acesso ao sistema de monitoramento';
COMMENT ON COLUMN usuario.password_hash IS 'Hash BCrypt da senha; nunca armazenar texto puro';
