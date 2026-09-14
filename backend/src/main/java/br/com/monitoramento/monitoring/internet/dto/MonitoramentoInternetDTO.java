package br.com.monitoramento.monitoring.internet.dto;

import br.com.monitoramento.monitoring.internet.entity.StatusInternet;

import java.time.OffsetDateTime;

public class MonitoramentoInternetDTO {

    private Long id;
    private OffsetDateTime dataHora;
    private StatusInternet status;
    private Long tempoResposta;
    private String mensagemErro;

    public MonitoramentoInternetDTO(Long id, OffsetDateTime dataHora, StatusInternet status,
                                     Long tempoResposta, String mensagemErro) {
        this.id = id;
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
