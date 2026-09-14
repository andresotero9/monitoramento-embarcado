-- Nova configuração, adicionada durante a implementação do monitoramento de câmeras
-- (Etapa 6): caminho RTSP a ser anexado à URL de conexão com a câmera.
-- A tabela CAMERA não possui um campo de "caminho RTSP" pois esse valor varia
-- por fabricante; manteve-se como configuração global editável em runtime,
-- evitando alterar o schema de CAMERA definido na Etapa 2.
INSERT INTO configuracao (chave, valor, tipo, descricao) VALUES
    ('camera.rtsp.caminho', '/', 'STRING',
     'Caminho anexado à URL RTSP das câmeras (ex.: /Streaming/Channels/101), configurável por padrão do fabricante utilizado');
