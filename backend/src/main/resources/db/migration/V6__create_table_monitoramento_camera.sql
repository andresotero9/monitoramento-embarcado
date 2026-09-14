-- Histórico de verificações periódicas de cada câmera cadastrada.
-- Uma câmera só é considerada ONLINE quando PING OK + RTSP OK + FRAME capturado.
CREATE TABLE monitoramento_camera (
    id                BIGSERIAL PRIMARY KEY,
    camera_id         BIGINT NOT NULL,
    data_hora         TIMESTAMPTZ NOT NULL DEFAULT now(),
    status            VARCHAR(20) NOT NULL,
    tempo_ping        BIGINT,
    frame_capturado   BOOLEAN NOT NULL DEFAULT FALSE,
    mensagem_erro     VARCHAR(500),

    CONSTRAINT fk_monitoramento_camera_camera
        FOREIGN KEY (camera_id) REFERENCES camera (id) ON DELETE CASCADE,
    CONSTRAINT ck_monitoramento_camera_status CHECK (status IN ('ONLINE', 'OFFLINE', 'INATIVA'))
);

CREATE INDEX idx_monitoramento_camera_camera_id_data_hora
    ON monitoramento_camera (camera_id, data_hora DESC);

COMMENT ON TABLE monitoramento_camera IS 'Histórico de verificações periódicas de câmeras (ping + RTSP + frame)';
COMMENT ON COLUMN monitoramento_camera.status IS 'ONLINE somente quando ping OK e frame RTSP capturado; INATIVA quando camera.ativa = false';
