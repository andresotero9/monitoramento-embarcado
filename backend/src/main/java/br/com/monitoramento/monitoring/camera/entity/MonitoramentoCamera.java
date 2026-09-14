package br.com.monitoramento.monitoring.camera.entity;

import br.com.monitoramento.camera.entity.Camera;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Registro de uma verificação de status de uma câmera (ping + RTSP + frame).
 */
@Entity
@Table(name = "monitoramento_camera")
public class MonitoramentoCamera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCamera status;

    @Column(name = "tempo_ping")
    private Long tempoPing;

    @Column(name = "frame_capturado", nullable = false)
    private boolean frameCapturado;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    protected MonitoramentoCamera() {
        // Construtor exigido pelo JPA
    }

    public MonitoramentoCamera(Camera camera, OffsetDateTime dataHora, StatusCamera status,
                                Long tempoPing, boolean frameCapturado, String mensagemErro) {
        this.camera = camera;
        this.dataHora = dataHora;
        this.status = status;
        this.tempoPing = tempoPing;
        this.frameCapturado = frameCapturado;
        this.mensagemErro = mensagemErro;
    }

    public Long getId() {
        return id;
    }

    public Camera getCamera() {
        return camera;
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
