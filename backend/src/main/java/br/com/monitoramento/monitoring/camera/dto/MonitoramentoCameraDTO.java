package br.com.monitoramento.monitoring.camera.dto;

import br.com.monitoramento.monitoring.camera.entity.StatusCamera;

import java.time.OffsetDateTime;

public class MonitoramentoCameraDTO {

    private Long id;
    private Long cameraId;
    private String cameraNome;
    private OffsetDateTime dataHora;
    private StatusCamera status;
    private Long tempoPing;
    private boolean frameCapturado;
    private String mensagemErro;

    public MonitoramentoCameraDTO(Long id, Long cameraId, String cameraNome, OffsetDateTime dataHora,
                                   StatusCamera status, Long tempoPing, boolean frameCapturado,
                                   String mensagemErro) {
        this.id = id;
        this.cameraId = cameraId;
        this.cameraNome = cameraNome;
        this.dataHora = dataHora;
        this.status = status;
        this.tempoPing = tempoPing;
        this.frameCapturado = frameCapturado;
        this.mensagemErro = mensagemErro;
    }

    public Long getId() {
        return id;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public String getCameraNome() {
        return cameraNome;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public StatusCamera getStatus() {
        return status;
    }

    public Long getTempoPing() {
        return tempoPing;
    }

    public boolean isFrameCapturado() {
        return frameCapturado;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}
