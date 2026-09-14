package br.com.monitoramento.monitoring.camera.service;

import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.repository.CameraRepository;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.monitoring.camera.CameraStreamChecker;
import br.com.monitoramento.monitoring.camera.PingChecker;
import br.com.monitoramento.monitoring.camera.PingResult;
import br.com.monitoramento.monitoring.camera.StreamCheckResult;
import br.com.monitoramento.monitoring.camera.entity.MonitoramentoCamera;
import br.com.monitoramento.monitoring.camera.entity.StatusCamera;
import br.com.monitoramento.monitoring.camera.repository.MonitoramentoCameraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Cobre os cenários da Seção 17: ping OK + RTSP OK, ping OK + RTSP falha,
 * ping falha, câmera recuperada e câmera inativa.
 *
 * <p>Regra central testada: uma câmera só é ONLINE quando ping E frame RTSP
 * são bem-sucedidos — nunca apenas com base no ping.</p>
 */
@ExtendWith(MockitoExtension.class)
class CameraMonitoringServiceTest {

    @Mock
    private MonitoramentoCameraRepository repository;
    @Mock
    private CameraRepository cameraRepository;
    @Mock
    private PingChecker pingChecker;
    @Mock
    private CameraStreamChecker cameraStreamChecker;
    @Mock
    private ConfiguracaoService configuracaoService;
    @Mock
    private AlertService alertService;

    private CameraMonitoringService service;

    @BeforeEach
    void configurar() {
        service = new CameraMonitoringService(
                repository, cameraRepository, pingChecker, cameraStreamChecker, configuracaoService, alertService);
        lenient().when(configuracaoService.getInt(eq("camera.ping.timeout.ms"), anyInt())).thenReturn(2000);
    }

    private Camera criarCameraAtiva() {
        return new Camera("Entrada", "Câmera da entrada", "192.168.0.10", 80, 554,
                "admin", "SENHA_CIFRADA", true);
    }

    @Test
    void pingOkRtspOk_deveFicarOnlineEResolverAlerta() {
        Camera camera = criarCameraAtiva();
        when(pingChecker.ping(eq("192.168.0.10"), anyInt())).thenReturn(PingResult.sucesso(15L));
        when(cameraStreamChecker.check(camera)).thenReturn(StreamCheckResult.sucesso());

        service.verificarCamera(camera);

        ArgumentCaptor<MonitoramentoCamera> captor = ArgumentCaptor.forClass(MonitoramentoCamera.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusCamera.ONLINE);
        assertThat(captor.getValue().isFrameCapturado()).isTrue();

        verify(alertService).resolverAlertaAberto(eq(TipoAlerta.CAMERA), anyString());
        verify(alertService, never()).registrarOuIgnorar(any(), any(), any(), any());
    }

    @Test
    void pingOkRtspFalha_deveFicarOfflineERegistrarAlerta() {
        Camera camera = criarCameraAtiva();
        when(pingChecker.ping(eq("192.168.0.10"), anyInt())).thenReturn(PingResult.sucesso(15L));
        when(cameraStreamChecker.check(camera)).thenReturn(StreamCheckResult.falha("Nenhum frame capturado"));

        service.verificarCamera(camera);

        ArgumentCaptor<MonitoramentoCamera> captor = ArgumentCaptor.forClass(MonitoramentoCamera.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusCamera.OFFLINE);
        assertThat(captor.getValue().isFrameCapturado()).isFalse();

        verify(alertService).registrarOuIgnorar(eq(TipoAlerta.CAMERA), anyString(), any(), anyString());
        verify(alertService, never()).resolverAlertaAberto(any(), any());
    }

    @Test
    void pingFalha_deveFicarOfflineSemNemTentarRtsp() {
        Camera camera = criarCameraAtiva();
        when(pingChecker.ping(eq("192.168.0.10"), anyInt())).thenReturn(PingResult.falha());

        service.verificarCamera(camera);

        verify(cameraStreamChecker, never()).check(any());

        ArgumentCaptor<MonitoramentoCamera> captor = ArgumentCaptor.forClass(MonitoramentoCamera.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusCamera.OFFLINE);

        verify(alertService).registrarOuIgnorar(eq(TipoAlerta.CAMERA), anyString(), any(), anyString());
    }

    @Test
    void cameraRecuperada_apesarDeFalhaAnterior_deveResolverAlertaAoVoltarOnline() {
        Camera camera = criarCameraAtiva();

        when(pingChecker.ping(eq("192.168.0.10"), anyInt()))
                .thenReturn(PingResult.falha())
                .thenReturn(PingResult.sucesso(20L));
        when(cameraStreamChecker.check(camera)).thenReturn(StreamCheckResult.sucesso());

        service.verificarCamera(camera); // 1ª verificação: offline (ping falhou)
        service.verificarCamera(camera); // 2ª verificação: recupera (ping + RTSP OK)

        verify(alertService).registrarOuIgnorar(eq(TipoAlerta.CAMERA), anyString(), any(), anyString());
        verify(alertService).resolverAlertaAberto(eq(TipoAlerta.CAMERA), anyString());
    }

    @Test
    void cameraInativa_naoDeveVerificarNemGerarAlerta() {
        Camera camera = new Camera("Depósito", null, "192.168.0.20", null, 554,
                null, null, false); // ativa = false

        service.verificarCamera(camera);

        verify(pingChecker, never()).ping(any(), anyInt());
        verify(cameraStreamChecker, never()).check(any());
        verify(alertService, never()).registrarOuIgnorar(any(), any(), any(), any());
        verify(alertService, never()).resolverAlertaAberto(any(), any());

        ArgumentCaptor<MonitoramentoCamera> captor = ArgumentCaptor.forClass(MonitoramentoCamera.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusCamera.INATIVA);
    }
}
