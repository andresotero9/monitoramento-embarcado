package br.com.monitoramento.monitoring.disk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Registro de uma verificação de uso de disco do filesystem Linux.
 */
@Entity
@Table(name = "monitoramento_disco")
public class MonitoramentoDisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "espaco_total", nullable = false)
    private Long espacoTotal;

    @Column(name = "espaco_utilizado", nullable = false)
    private Long espacoUtilizado;

    @Column(name = "espaco_livre", nullable = false)
    private Long espacoLivre;

    @Column(name = "percentual_utilizado", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualUtilizado;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    protected MonitoramentoDisco() {
        // Construtor exigido pelo JPA
    }

    public MonitoramentoDisco(Long espacoTotal, Long espacoUtilizado, Long espacoLivre,
                               BigDecimal percentualUtilizado, OffsetDateTime dataHora) {
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
