package br.com.monitoramento.config_sistema.entity;

/**
 * Tipo do valor armazenado em {@link Configuracao#getValor()}, usado para
 * conversão correta na camada de serviço.
 */
public enum TipoConfiguracao {
    STRING,
    INTEGER,
    BOOLEAN,
    DECIMAL
}
