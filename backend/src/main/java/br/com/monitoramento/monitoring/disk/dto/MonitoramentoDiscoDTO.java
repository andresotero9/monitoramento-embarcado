package br.com.monitoramento.monitoring.disk.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class MonitoramentoDiscoDTO {

    private Long id;
    private Long espacoTotal;
    private Long espacoUtilizado;
    private Long espacoLivre;
    private BigDecimal percentualUtilizado;
    private OffsetDateTime dataHora;

    public MonitoramentoDiscoDTO(Long id, Long espacoTotal, Long espacoUtilizado, Long espacoLivre,
                                  BigDecimal percentualUtilizado, OffsetDateTime dataHora) {
        this.id = id;
        this.espacoTotal = espacoTotal;
        this.espacoUtilizado = espacoUtilizado;
        this.espacoLivre = espacoLivre;
        this.percentualUtilizado = percentualUtilizado;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Long getEspacoTotal() {
        return espacoTotal;
    }

    public Long getEspacoUtilizado() {
        return espacoUtilizado;
    }

    public Long getEspacoLivre() {
        return espacoLivre;
    }

    public BigDecimal getPercentualUtilizado() {
        return percentualUtilizado;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }
}
