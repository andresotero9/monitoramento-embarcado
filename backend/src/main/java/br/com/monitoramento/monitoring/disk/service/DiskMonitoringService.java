package br.com.monitoramento.monitoring.disk.service;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.exception.ResourceNotFoundException;
import br.com.monitoramento.monitoring.disk.DiskSpaceInfo;
import br.com.monitoramento.monitoring.disk.DiskSpaceProvider;
import br.com.monitoramento.monitoring.disk.dto.MonitoramentoDiscoDTO;
import br.com.monitoramento.monitoring.disk.entity.MonitoramentoDisco;
import br.com.monitoramento.monitoring.disk.repository.MonitoramentoDiscoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consulta ao histórico de uso de disco, e execução periódica da leitura
 * (chamada pelo {@code DiskMonitoringScheduler}).
 *
 * <p>Quando o percentual de uso ultrapassa o limite configurado, um alerta é
 * criado uma única vez (deduplicado por {@link AlertService}); quando o uso volta
 * ao normal, o alerta aberto é resolvido automaticamente, registrando o evento
 * de recuperação.</p>
 */
@Service
public class DiskMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(DiskMonitoringService.class);

    private static final String CHAVE_LIMITE_PERCENTUAL = "disco.limite.alerta.percentual";
    private static final String CHAVE_PERIODICIDADE_SEGUNDOS = "disco.periodicidade.segundos";
    private static final String CHAVE_PATH_MONITORADO = "disco.path.monitorado";

    private final MonitoramentoDiscoRepository repository;
    private final DiskSpaceProvider diskSpaceProvider;
    private final ConfiguracaoService configuracaoService;
    private final AlertService alertService;

    private final AtomicReference<Instant> ultimaExecucao = new AtomicReference<>(Instant.EPOCH);
    private final AtomicBoolean emExecucao = new AtomicBoolean(false);

    public DiskMonitoringService(MonitoramentoDiscoRepository repository,
                                  DiskSpaceProvider diskSpaceProvider,
                                  ConfiguracaoService configuracaoService,
                                  AlertService alertService) {
        this.repository = repository;
        this.diskSpaceProvider = diskSpaceProvider;
        this.configuracaoService = configuracaoService;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public Page<MonitoramentoDiscoDTO> listar(Pageable pageable) {
        return repository.findAllByOrderByDataHoraDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public MonitoramentoDiscoDTO buscarUltimo() {
        MonitoramentoDisco ultimo = repository.findFirstByOrderByDataHoraDesc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum registro de monitoramento de Disco encontrado ainda"));
        return toDTO(ultimo);
    }

    /**
     * Chamado periodicamente pelo scheduler. Só executa a verificação real quando
     * o intervalo configurado já passou desde a última execução, e apenas se não
     * houver outra execução em andamento.
     */
    public void executarRotina() {
        int periodicidadeSegundos = configuracaoService.getInt(CHAVE_PERIODICIDADE_SEGUNDOS, 300);
        Instant agora = Instant.now();

        // Se a última execução foi há menos de "periodicidadeSegundos", ignora a execução atual
        if (agora.isBefore(ultimaExecucao.get().plusSeconds(periodicidadeSegundos))) {
            return;
        }

        // Se já houver outra execução em andamento, ignora a execução atual
        if (!emExecucao.compareAndSet(false, true)) {
            log.debug("Verificação de Disco já em andamento; execução atual ignorada");
            return;
        }

        // Executa a verificação real, garantindo que o flag "emExecucao" seja resetado mesmo em caso de erro
        try {
            executarVerificacao();
            ultimaExecucao.set(agora);
        } finally {
            emExecucao.set(false);
        }
    }

    @Transactional
    void executarVerificacao() {
        String path = configuracaoService.getString(CHAVE_PATH_MONITORADO, "/");
        BigDecimal limitePercentual = configuracaoService.getDecimal(
                CHAVE_LIMITE_PERCENTUAL, BigDecimal.valueOf(85));

        log.info("Iniciando verificação de uso de disco (path={}, limite={}%)", path, limitePercentual);

        DiskSpaceInfo info = diskSpaceProvider.lerUsoDisco(path);
        BigDecimal percentualUtilizado = info.percentualUtilizado();

        MonitoramentoDisco registro = new MonitoramentoDisco(
                info.espacoTotal(), info.espacoUtilizado(), info.espacoLivre(),
                percentualUtilizado, OffsetDateTime.now());
        repository.save(registro);

        if (percentualUtilizado.compareTo(limitePercentual) > 0) {
            alertService.registrarOuIgnorar(TipoAlerta.DISCO, AlertService.ORIGEM_GLOBAL, NivelAlerta.WARNING,
                    "Uso de disco em " + percentualUtilizado + "%, acima do limite de " + limitePercentual + "%");
            log.warn("Uso de disco acima do limite: {}% (limite {}%)", percentualUtilizado, limitePercentual);
        } else {
            alertService.resolverAlertaAberto(TipoAlerta.DISCO, AlertService.ORIGEM_GLOBAL);
        }

        log.info("Verificação de Disco concluída (percentual={}%)", percentualUtilizado);
    }

    private MonitoramentoDiscoDTO toDTO(MonitoramentoDisco m) {
        return new MonitoramentoDiscoDTO(
                m.getId(), m.getEspacoTotal(), m.getEspacoUtilizado(), m.getEspacoLivre(),
                m.getPercentualUtilizado(), m.getDataHora());
    }
}

