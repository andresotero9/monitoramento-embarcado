-- Tabela de configurações do sistema no modelo chave/valor tipado.
-- Evita alterações de esquema a cada novo parâmetro de monitoramento.
CREATE TABLE configuracao (
    id               BIGSERIAL PRIMARY KEY,
    chave            VARCHAR(100) NOT NULL,
    valor            VARCHAR(500) NOT NULL,
    tipo             VARCHAR(20) NOT NULL,
    descricao        VARCHAR(255),
    data_atualizacao TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_configuracao_chave UNIQUE (chave),
    CONSTRAINT ck_configuracao_tipo CHECK (tipo IN ('STRING', 'INTEGER', 'BOOLEAN', 'DECIMAL'))
);

COMMENT ON TABLE configuracao IS 'Configurações do sistema em modelo chave/valor tipado';
COMMENT ON COLUMN configuracao.tipo IS 'Tipo do valor para conversão correta na camada de serviço';
