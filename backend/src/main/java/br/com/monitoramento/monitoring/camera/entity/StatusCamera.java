package br.com.monitoramento.monitoring.camera.entity;

/**
 * Status de uma câmera em um instante de verificação.
 *
 * <p>{@code ONLINE} só é atribuído quando ping e captura de frame RTSP
 * são bem-sucedidos simultaneamente (ver {@code CameraMonitoringService}).</p>
 */
public enum StatusCamera {
    ONLINE,
    OFFLINE,
    INATIVA
}
