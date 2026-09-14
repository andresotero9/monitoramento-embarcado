-- Alertas gerados pelo sistema de monitoramento.
-- "origem" identifica a fonte do alerta: id da câmera (como texto) para tipo CAMERA,
-- ou 'GLOBAL' para INTERNET/DISCO/SISTEMA.
-- A combinação (tipo, origem, resolvido = false) é única por índice parcial,
-- garantindo que não existam alertas abertos duplicados para a mesma origem/tipo.
CREATE TABLE alerta (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(20) NOT NULL,
    origem          VARCHAR(100) NOT NULL,
    nivel           VARCHAR(20) NOT NULL,
    mensagem        VARCHAR(500) NOT NULL,
    data_hora       TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolvido       BOOLEAN NOT NULL DEFAULT FALSE,
    data_resolucao  TIMESTAMPTZ,

    CONSTRAINT ck_alerta_tipo CHECK (tipo IN ('INTERNET', 'DISCO', 'CAMERA', 'SISTEMA')),
    CONSTRAINT ck_alerta_nivel CHECK (nivel IN ('INFO', 'WARNING', 'CRITICAL'))
);

-- Índice parcial: garante que não existam 2 alertas ABERTOS para a mesma origem/tipo,
-- implementando a deduplicação diretamente no banco (defesa em profundidade,
-- além da checagem feita no AlertService).
CREATE UNIQUE INDEX uk_alerta_aberto_por_origem
    ON alerta (tipo, origem)
    WHERE resolvido = FALSE;

CREATE INDEX idx_alerta_data_hora ON alerta (data_hora DESC);
CREATE INDEX idx_alerta_resolvido ON alerta (resolvido);

COMMENT ON TABLE alerta IS 'Alertas gerados pelo sistema de monitoramento';
COMMENT ON COLUMN alerta.origem IS 'Identificador da origem: id da câmera ou GLOBAL para internet/disco/sistema';
