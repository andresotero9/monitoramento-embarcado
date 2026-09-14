-- Usuário administrador padrão.
-- Senha: admin (hash BCrypt abaixo). ATENÇÃO: trocar em ambiente real (ver README).
INSERT INTO usuario (username, password_hash, role, ativo)
VALUES ('admin', '$2b$12$OihNUy2MKDKL9iArsqt2Wehicx7n2Ihiwxzb0mp.3YeQpunYQm8Y.', 'ADMIN', TRUE);

-- Configurações padrão de monitoramento de Internet.
INSERT INTO configuracao (chave, valor, tipo, descricao) VALUES
    ('internet.ip.teste', '8.8.8.8', 'STRING', 'Endereço IP utilizado para testar conectividade com a Internet'),
    ('internet.timeout.ms', '3000', 'INTEGER', 'Timeout em milissegundos para o teste de conectividade'),
    ('internet.periodicidade.segundos', '60', 'INTEGER', 'Intervalo entre execuções do monitoramento de Internet');

-- Configurações padrão de monitoramento de Disco.
INSERT INTO configuracao (chave, valor, tipo, descricao) VALUES
    ('disco.limite.alerta.percentual', '85', 'DECIMAL', 'Percentual de uso de disco a partir do qual um alerta é gerado'),
    ('disco.periodicidade.segundos', '300', 'INTEGER', 'Intervalo entre execuções do monitoramento de Disco'),
    ('disco.path.monitorado', '/', 'STRING', 'Caminho do filesystem a ser monitorado');

-- Configurações padrão de monitoramento de Câmeras.
INSERT INTO configuracao (chave, valor, tipo, descricao) VALUES
    ('camera.periodicidade.segundos', '60', 'INTEGER', 'Intervalo entre execuções do monitoramento de Câmeras'),
    ('camera.ping.timeout.ms', '2000', 'INTEGER', 'Timeout em milissegundos para o teste de ping'),
    ('camera.rtsp.timeout.ms', '5000', 'INTEGER', 'Timeout em milissegundos para captura de frame via RTSP/FFmpeg');
