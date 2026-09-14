-- Histórico de verificações periódicas de uso de disco do filesystem Linux.
CREATE TABLE monitoramento_disco (
    id                    BIGSERIAL PRIMARY KEY,
    espaco_total          BIGINT NOT NULL,
    espaco_utilizado      BIGINT NOT NULL,
    espaco_livre          BIGINT NOT NULL,
    percentual_utilizado  NUMERIC(5,2) NOT NULL,
    data_hora             TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_monitoramento_disco_percentual CHECK (percentual_utilizado BETWEEN 0 AND 100)
);

CREATE INDEX idx_monitoramento_disco_data_hora ON monitoramento_disco (data_hora DESC);

COMMENT ON TABLE monitoramento_disco IS 'Histórico de verificações periódicas de uso de disco (bytes)';
