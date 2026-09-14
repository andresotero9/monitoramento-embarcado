-- Histórico de verificações periódicas de conectividade com a Internet.
CREATE TABLE monitoramento_internet (
    id              BIGSERIAL PRIMARY KEY,
    data_hora       TIMESTAMPTZ NOT NULL DEFAULT now(),
    status          VARCHAR(20) NOT NULL,
    tempo_resposta  BIGINT,
    mensagem_erro   VARCHAR(500),

    CONSTRAINT ck_monitoramento_internet_status CHECK (status IN ('ONLINE', 'OFFLINE'))
);

CREATE INDEX idx_monitoramento_internet_data_hora ON monitoramento_internet (data_hora DESC);

COMMENT ON TABLE monitoramento_internet IS 'Histórico de verificações periódicas de conectividade com a Internet';
COMMENT ON COLUMN monitoramento_internet.tempo_resposta IS 'Tempo de resposta em milissegundos; nulo quando OFFLINE';
