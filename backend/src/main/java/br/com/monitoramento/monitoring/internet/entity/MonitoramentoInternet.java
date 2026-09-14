package br.com.monitoramento.monitoring.internet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Registro de uma verificação de conectividade com a Internet.
 */
@Entity
@Table(name = "monitoramento_internet")
public class MonitoramentoInternet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInternet status;

    @Column(name = "tempo_resposta")
    private Long tempoResposta;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    protected MonitoramentoInternet() {
        // Construtor exigido pelo JPA
    }

    public MonitoramentoInternet(OffsetDateTime dataHora, StatusInternet status,
                                 Long tempoResposta, String mensagemErro) {
        this.dataHora = dataHora;
        this.status = status;
        this.tempoResposta = tempoResposta;
        this.mensagemErro = mensagemErro;
    }

    public Long getId() {
        return id;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public StatusInternet getStatus() {
        return status;
    }

    public Long getTempoResposta() {
        return tempoResposta;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}
