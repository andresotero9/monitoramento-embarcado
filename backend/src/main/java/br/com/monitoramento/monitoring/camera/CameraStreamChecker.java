package br.com.monitoramento.monitoring.camera;

import br.com.monitoramento.camera.entity.Camera;

/**
 * Abstrai a tentativa de capturar um frame via RTSP de uma câmera, permitindo
 * testar {@code CameraMonitoringService} sem depender de uma câmera física ou
 * do FFmpeg — assinatura conforme especificada na Seção 29 dos requisitos.
 *
 * <p>A implementação real é responsável por decifrar a senha da câmera e ler o
 * timeout configurado; a assinatura enxuta ({@code Camera} apenas) mantém a
 * interface de negócio simples para os testes unitários.</p>
 */
public interface CameraStreamChecker {

    StreamCheckResult check(Camera camera);
}
