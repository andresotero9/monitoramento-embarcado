package br.com.monitoramento.monitoring.internet.service;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.exception.ResourceNotFoundException;
import br.com.monitoramento.monitoring.internet.InternetCheckResult;
import br.com.monitoramento.monitoring.internet.InternetChecker;
import br.com.monitoramento.monitoring.internet.dto.MonitoramentoInternetDTO;
import br.com.monitoramento.monitoring.internet.entity.MonitoramentoInternet;
import br.com.monitoramento.monitoring.internet.entity.StatusInternet;
import br.com.monitoramento.monitoring.internet.repository.MonitoramentoInternetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consulta ao histórico de verificações de conectividade com a Internet, e execução
 * periódica do teste de conectividade (chamada pelo {@code InternetMonitoringScheduler}).
 *
 * <p>A periodicidade é lida da configuração a cada execução do scheduler (ver
 * {@link #executarRotina()}), permitindo alterar o intervalo em
 * runtime via {@code PUT /api/configuracoes} sem reiniciar a aplicação. Um
 * {@link AtomicBoolean} evita que duas execuções da mesma rotina rodem em paralelo
 * caso uma verificação demore mais que o intervalo configurado.</p>
 */
@Service
public class InternetMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(InternetMonitoringService.class);

    private static final String CHAVE_IP_TESTE = "internet.ip.teste";
    private static final String CHAVE_TIMEOUT_MS = "internet.timeout.ms";
    private static final String CHAVE_PERIODICIDADE_SEGUNDOS = "internet.periodicidade.segundos";

    private final MonitoramentoInternetRepository repository;
    private final InternetChecker internetChecker;
    private final ConfiguracaoService configuracaoService;
    private final AlertService alertService;

    private final AtomicReference<Instant> ultimaExecucao = new AtomicReference<>(Instant.EPOCH);
    private final AtomicBoolean emExecucao = new AtomicBoolean(false);

    public InternetMonitoringService(MonitoramentoInternetRepository repository,
                                     InternetChecker internetChecker,
                                     ConfiguracaoService configuracaoService,
                                     AlertService alertService) {
        this.repository = repository;
        this.internetChecker = internetChecker;
        this.configuracaoService = configuracaoService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public Page<MonitoramentoInternetDTO> listar(Pageable pageable) {
        return repository.findAllByOrderByDataHoraDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public MonitoramentoInternetDTO buscarUltimo() {
        MonitoramentoInternet ultimo = repository.findFirstByOrderByDataHoraDesc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum registro de monitoramento de Internet encontrado ainda"));
        return toDTO(ultimo);
    }

    /**
     * Chamado periodicamente pelo scheduler. Só executa a verificação real quando
     * o intervalo configurado ({@code internet.periodicidade.segundos}) já passou
     * desde a última execução, e apenas se não houver outra execução em andamento.
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
            log.debug("Verificação de Internet já em andamento; execução atual ignorada");
            return;
        }

        // Executa a verificação real de conectividade com a Internet, salvando o resultado
        try {
            executarVerificacao();
            ultimaExecucao.set(agora);
        } finally {
            emExecucao.set(false);
        }
    }

    /**
     * Executa a verificação real de conectividade com a Internet, salvando o resultado
     * no banco e registrando ou resolvendo alertas conforme o status.
     */
    @Transactional
    void executarVerificacao() {
        String ipTeste = configuracaoService.getString(CHAVE_IP_TESTE, "8.8.8.8");
        int timeoutMs = configuracaoService.getInt(CHAVE_TIMEOUT_MS, 3000);

        log.info("Iniciando verificação de conectividade com a Internet (ip={}, timeout={}ms)",
                ipTeste, timeoutMs);

        InternetCheckResult resultado = internetChecker.check(ipTeste, timeoutMs);

        MonitoramentoInternet registro = new MonitoramentoInternet(
                OffsetDateTime.now(),
                resultado.sucesso() ? StatusInternet.ONLINE : StatusInternet.OFFLINE,
                resultado.tempoRespostaMs(),
                resultado.mensagemErro());
        repository.save(registro);

        if (resultado.sucesso()) {
            alertService.resolverAlertaAberto(TipoAlerta.INTERNET, AlertService.ORIGEM_GLOBAL);
        } else {
            alertService.registrarOuIgnorar(TipoAlerta.INTERNET, AlertService.ORIGEM_GLOBAL,
                    NivelAlerta.CRITICAL, "Internet indisponível: " + resultado.mensagemErro());
            log.warn("Falha na verificação de Internet: {}", resultado.mensagemErro());
        }

        log.info("Verificação de Internet concluída (status={})", registro.getStatus());
    }

    private MonitoramentoInternetDTO toDTO(MonitoramentoInternet m) {
        return new MonitoramentoInternetDTO(
                m.getId(), m.getDataHora(), m.getStatus(), m.getTempoResposta(), m.getMensagemErro());
    }
}

