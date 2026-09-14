package br.com.monitoramento.monitoring.disk.service;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.monitoring.disk.DiskSpaceInfo;
import br.com.monitoramento.monitoring.disk.DiskSpaceProvider;
import br.com.monitoramento.monitoring.disk.entity.MonitoramentoDisco;
import br.com.monitoramento.monitoring.disk.repository.MonitoramentoDiscoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Cobre os cenários da Seção 17: disco normal, acima do limite, recuperação e
 * não duplicação de alertas (delegada ao {@link AlertService}, já testado
 * isoladamente em {@code AlertServiceTest}).
 */
@ExtendWith(MockitoExtension.class)
class DiskMonitoringServiceTest {

    private static final String PATH = "/";

    @Mock
    private MonitoramentoDiscoRepository repository;
    @Mock
    private DiskSpaceProvider diskSpaceProvider;
    @Mock
    private ConfiguracaoService configuracaoService;
    @Mock
    private AlertService alertService;

    private DiskMonitoringService service;

    @BeforeEach
    void configurar() {
        service = new DiskMonitoringService(repository, diskSpaceProvider, configuracaoService, alertService);
        when(configuracaoService.getString(eq("disco.path.monitorado"), anyString())).thenReturn(PATH);
        when(configuracaoService.getDecimal(eq("disco.limite.alerta.percentual"), any()))
                .thenReturn(BigDecimal.valueOf(85));
    }

    @Test
    void discoNormal_naoDeveGerarAlertaEDeveResolverAbertoSeHouver() {
        when(diskSpaceProvider.lerUsoDisco(PATH)).thenReturn(new DiskSpaceInfo(1000L, 500L, 500L)); // 50%

        service.executarVerificacao();

        ArgumentCaptor<MonitoramentoDisco> captor = ArgumentCaptor.forClass(MonitoramentoDisco.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPercentualUtilizado()).isEqualByComparingTo("50.00");

        verify(alertService).resolverAlertaAberto(TipoAlerta.DISCO, AlertService.ORIGEM_GLOBAL);
        verify(alertService, never()).registrarOuIgnorar(any(), any(), any(), any());
    }

    @Test
    void discoAcimaDoLimite_deveRegistrarAlertaWarning() {
        when(diskSpaceProvider.lerUsoDisco(PATH)).thenReturn(new DiskSpaceInfo(1000L, 900L, 100L)); // 90%

        service.executarVerificacao();

        verify(alertService).registrarOuIgnorar(
                eq(TipoAlerta.DISCO), eq(AlertService.ORIGEM_GLOBAL), eq(NivelAlerta.WARNING), anyString());
        verify(alertService, never()).resolverAlertaAberto(any(), any());
    }

    @Test
    void recuperacao_apesarDeAlertaAnterior_deveResolverQuandoVoltaAoNormal() {
        when(diskSpaceProvider.lerUsoDisco(PATH))
                .thenReturn(new DiskSpaceInfo(1000L, 900L, 100L))  // 1ª verificação: 90%, acima do limite
                .thenReturn(new DiskSpaceInfo(1000L, 500L, 500L)); // 2ª verificação: 50%, normal novamente

        service.executarVerificacao();
        service.executarVerificacao();

        verify(alertService).registrarOuIgnorar(eq(TipoAlerta.DISCO), eq(AlertService.ORIGEM_GLOBAL), any(), anyString());
        verify(alertService).resolverAlertaAberto(TipoAlerta.DISCO, AlertService.ORIGEM_GLOBAL);
    }

    @Test
    void naoDuplicacao_serviceSempreDelegaAoAlertServiceQueGaranteDedup() {
        when(diskSpaceProvider.lerUsoDisco(PATH)).thenReturn(new DiskSpaceInfo(1000L, 900L, 100L)); // 90%, sempre acima

        service.executarVerificacao();
        service.executarVerificacao();

        // O service não tenta ele mesmo evitar duplicidade — delega a cada chamada;
        // é o AlertService (testado isoladamente) quem garante que só um alerta fique aberto.
        verify(alertService, times(2)).registrarOuIgnorar(
                eq(TipoAlerta.DISCO), eq(AlertService.ORIGEM_GLOBAL), eq(NivelAlerta.WARNING), anyString());
    }
}
