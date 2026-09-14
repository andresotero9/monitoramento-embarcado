package br.com.monitoramento.monitoring.camera.service;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.repository.CameraRepository;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.exception.ResourceNotFoundException;
import br.com.monitoramento.monitoring.camera.CameraStreamChecker;
import br.com.monitoramento.monitoring.camera.PingChecker;
import br.com.monitoramento.monitoring.camera.PingResult;
import br.com.monitoramento.monitoring.camera.StreamCheckResult;
import br.com.monitoramento.monitoring.camera.dto.MonitoramentoCameraDTO;
import br.com.monitoramento.monitoring.camera.entity.MonitoramentoCamera;
import br.com.monitoramento.monitoring.camera.entity.StatusCamera;
import br.com.monitoramento.monitoring.camera.repository.MonitoramentoCameraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consulta ao histórico de verificações de câmeras, e execução periódica do
 * monitoramento (chamada pelo {@code CameraMonitoringScheduler}).
 *
 * <p><b>Regra central:</b> uma câmera só é considerada {@link StatusCamera#ONLINE}
 * quando o ping responde <em>e</em> um frame RTSP é capturado com sucesso — nunca
 * apenas com base no ping (ver Seção 29 dos requisitos). Câmeras com
 * {@code ativa = false} são registradas como {@link StatusCamera#INATIVA} e não
 * geram alertas.</p>
 */
@Service
public class CameraMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(CameraMonitoringService.class);
    private static final String CHAVE_PERIODICIDADE_SEGUNDOS = "camera.periodicidade.segundos";
    private static final String CHAVE_PING_TIMEOUT_MS = "camera.ping.timeout.ms";

    private final MonitoramentoCameraRepository repository;
    private final CameraRepository cameraRepository;
    private final PingChecker pingChecker;
    private final CameraStreamChecker cameraStreamChecker;
    private final ConfiguracaoService configuracaoService;
    private final AlertService alertService;

    private final AtomicReference<Instant> ultimaExecucao = new AtomicReference<>(Instant.EPOCH);
    private final AtomicBoolean emExecucao = new AtomicBoolean(false);

    public CameraMonitoringService(MonitoramentoCameraRepository repository, CameraRepository cameraRepository, PingChecker pingChecker, CameraStreamChecker cameraStreamChecker, ConfiguracaoService configuracaoService, AlertService alertService) {
        this.repository = repository;
        this.cameraRepository = cameraRepository;
        this.pingChecker = pingChecker;
        this.cameraStreamChecker = cameraStreamChecker;
        this.configuracaoService = configuracaoService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public Page<MonitoramentoCameraDTO> listarTodas(Pageable pageable) {
        return repository.findAllByOrderByDataHoraDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<MonitoramentoCameraDTO> listarPorCamera(Long cameraId, Pageable pageable) {
        return repository.findAllByCameraIdOrderByDataHoraDesc(cameraId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public MonitoramentoCameraDTO buscarUltimoPorCamera(Long cameraId) {
        MonitoramentoCamera ultimo = repository.findFirstByCameraIdOrderByDataHoraDesc(cameraId).orElseThrow(() -> new ResourceNotFoundException("Nenhum registro de monitoramento encontrado para a câmera id=" + cameraId));
        return toDTO(ultimo);
    }

    /**
     * Chamado periodicamente pelo scheduler. Só executa a verificação real quando
     * o intervalo configurado já passou desde a última execução, e apenas se não
     * houver outra execução em andamento.
     */
    public void executarRotina() {
        int periodicidadeSegundos = configuracaoService.getInt(CHAVE_PERIODICIDADE_SEGUNDOS, 60);
        Instant agora = Instant.now();

        // Se a última execução foi há menos de "periodicidadeSegundos", ignora a execução atual
        if (agora.isBefore(ultimaExecucao.get().plusSeconds(periodicidadeSegundos))) {
            return;
        }

        // Se já houver outra execução em andamento, ignora a execução atual
        if (!emExecucao.compareAndSet(false, true)) {
            log.debug("Verificação de Câmeras já em andamento; execução atual ignorada");
            return;
        }

        // Executa a verificação real de todas as câmeras, garantindo que o flag "emExecucao" seja resetado mesmo em caso de erro
        try {
            executarVerificacao();
            ultimaExecucao.set(agora);
        } finally {
            emExecucao.set(false);
        }
    }

    /**
     * Executa a verificação de todas as câmeras cadastradas, registrando o status
     * de cada uma no histórico e gerando alertas quando necessário.
     */
    void executarVerificacao() {
        List<Camera> cameras = cameraRepository.findAll();
        log.info("Iniciando verificação de {} câmera(s) cadastrada(s)", cameras.size());
        for (Camera camera : cameras) {
            try {
                verificarCamera(camera);
            } catch (Exception e) {
                // Falha ao verificar uma câmera não deve interromper a verificação das demais.
                log.error("Erro inesperado ao verificar câmera id={}: {}", camera.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Verifica o status de uma câmera específica.
     *
     * @param camera
     */
    @Transactional
    void verificarCamera(Camera camera) {
        String origem = String.valueOf(camera.getId());

        // Câmeras inativas não são verificadas, mas é registrado o status INATIVA no histórico.
        if (!camera.isAtiva()) {
            registrar(camera, StatusCamera.INATIVA, null, false, null);
            return;
        }

        int pingTimeoutMs = configuracaoService.getInt(CHAVE_PING_TIMEOUT_MS, 2000);
        PingResult ping = pingChecker.ping(camera.getEnderecoIp(), pingTimeoutMs);

        if (!ping.sucesso()) {
            registrar(camera, StatusCamera.OFFLINE, null, false, "Ping falhou para " + camera.getEnderecoIp());
            alertService.registrarOuIgnorar(TipoAlerta.CAMERA, origem, NivelAlerta.CRITICAL, "Câmera '" + camera.getNome() + "' offline: ping não respondeu");
            return;
        }

        StreamCheckResult streamResult = cameraStreamChecker.check(camera);

        if (!streamResult.frameCapturado()) {
            registrar(camera, StatusCamera.OFFLINE, ping.tempoPingMs(), false, streamResult.mensagemErro());
            alertService.registrarOuIgnorar(TipoAlerta.CAMERA, origem, NivelAlerta.CRITICAL, "Câmera '" + camera.getNome() + "' offline: " + streamResult.mensagemErro());
            return;
        }

        // Ping OK + frame RTSP capturado: câmera está de fato ONLINE.
        registrar(camera, StatusCamera.ONLINE, ping.tempoPingMs(), true, null);
        alertService.resolverAlertaAberto(TipoAlerta.CAMERA, origem);
    }

    /**
     * Registra o resultado da verificação de uma câmera no histórico.
     *
     * @param camera
     * @param status
     * @param tempoPing
     * @param frameCapturado
     * @param mensagemErro
     */
    private void registrar(Camera camera, StatusCamera status, Long tempoPing, boolean frameCapturado, String mensagemErro) {
        MonitoramentoCamera registro = new MonitoramentoCamera(camera, OffsetDateTime.now(), status, tempoPing, frameCapturado, mensagemErro);
        repository.save(registro);
        log.info("Câmera '{}' (id={}) verificada: status={}", camera.getNome(), camera.getId(), status);
    }

    /**
     * Converte uma entidade de monitoramento de câmera em DTO para retorno via API.
     *
     * @param m
     * @return
     */
    private MonitoramentoCameraDTO toDTO(MonitoramentoCamera m) {
        return new MonitoramentoCameraDTO(m.getId(), m.getCamera().getId(), m.getCamera().getNome(), m.getDataHora(), m.getStatus(), m.getTempoPing(), m.isFrameCapturado(), m.getMensagemErro());
    }
}

