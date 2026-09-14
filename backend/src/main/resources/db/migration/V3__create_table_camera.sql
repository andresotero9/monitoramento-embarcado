-- Cadastro de câmeras IP monitoradas.
-- A senha é armazenada CRIPTOGRAFADA (AES, cifrada/decifrada na camada de serviço),
-- e não em hash, pois é necessário recuperar o valor original para autenticar no RTSP.
-- A senha em texto puro NUNCA é retornada pela API (ver CameraResponseDTO).
CREATE TABLE camera (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    descricao         VARCHAR(255),
    endereco_ip       VARCHAR(45) NOT NULL,
    porta_http        INTEGER,
    porta_rtsp        INTEGER NOT NULL DEFAULT 554,
    usuario           VARCHAR(100),
    senha_criptografada VARCHAR(500),
    ativa             BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao      TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_atualizacao  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_camera_porta_http CHECK (porta_http IS NULL OR (porta_http BETWEEN 1 AND 65535)),
    CONSTRAINT ck_camera_porta_rtsp CHECK (porta_rtsp BETWEEN 1 AND 65535)
);

CREATE INDEX idx_camera_ativa ON camera (ativa);

COMMENT ON TABLE camera IS 'Cadastro de câmeras IP monitoradas via RTSP';
COMMENT ON COLUMN camera.senha_criptografada IS 'Senha da câmera cifrada com AES; jamais exposta pela API';
