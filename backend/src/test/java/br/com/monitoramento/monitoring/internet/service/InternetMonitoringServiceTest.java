package br.com.monitoramento.monitoring.internet.service;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.service.AlertService;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import br.com.monitoramento.monitoring.internet.InternetCheckResult;
import br.com.monitoramento.monitoring.internet.InternetChecker;
import br.com.monitoramento.monitoring.internet.entity.MonitoramentoInternet;
import br.com.monitoramento.monitoring.internet.entity.StatusInternet;
import br.com.monitoramento.monitoring.internet.repository.MonitoramentoInternetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Cobre os cenários da Seção 17: Internet disponível, timeout e erro de conexão.
 */
@ExtendWith(MockitoExtension.class)
class InternetMonitoringServiceTest {

    @Mock
    private MonitoramentoInternetRepository repository;
    @Mock
    private InternetChecker internetChecker;
    @Mock
    private ConfiguracaoService configuracaoService;
    @Mock
    private AlertService alertService;

    private InternetMonitoringService service;

    @BeforeEach
    void configurar() {
        service = new InternetMonitoringService(repository, internetChecker, configuracaoService, alertService);
        when(configuracaoService.getString(eq("internet.ip.teste"), anyString())).thenReturn("8.8.8.8");
        when(configuracaoService.getInt(eq("internet.timeout.ms"), anyInt())).thenReturn(3000);
    }

    @Test
    void internetDisponivel_deveSalvarOnlineEResolverAlertaAberto() {
        when(internetChecker.check("8.8.8.8", 3000)).thenReturn(InternetCheckResult.sucesso(42L));

        service.executarVerificacao();

        ArgumentCaptor<MonitoramentoInternet> captor = ArgumentCaptor.forClass(MonitoramentoInternet.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusInternet.ONLINE);
        assertThat(captor.getValue().getTempoResposta()).isEqualTo(42L);
        assertThat(captor.getValue().getMensagemErro()).isNull();

        verify(alertService).resolverAlertaAberto(TipoAlerta.INTERNET, AlertService.ORIGEM_GLOBAL);
        verify(alertService, never()).registrarOuIgnorar(any(), any(), any(), any());
    }

    @Test
    void timeout_deveSalvarOfflineERegistrarAlertaCritico() {
        when(internetChecker.check("8.8.8.8", 3000))
                .thenReturn(InternetCheckResult.falha("Timeout ao conectar em 8.8.8.8"));

        service.executarVerificacao();

        ArgumentCaptor<MonitoramentoInternet> captor = ArgumentCaptor.forClass(MonitoramentoInternet.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusInternet.OFFLINE);
        assertThat(captor.getValue().getMensagemErro()).contains("Timeout");

        verify(alertService).registrarOuIgnorar(
                eq(TipoAlerta.INTERNET), eq(AlertService.ORIGEM_GLOBAL), eq(NivelAlerta.CRITICAL), anyString());
        verify(alertService, never()).resolverAlertaAberto(any(), any());
    }

    @Test
    void erroDeConexao_deveSalvarOfflineERegistrarAlertaCritico() {
        when(internetChecker.check("8.8.8.8", 3000))
                .thenReturn(InternetCheckResult.falha("Falha ao conectar em 8.8.8.8: Connection refused"));

        service.executarVerificacao();

        ArgumentCaptor<MonitoramentoInternet> captor = ArgumentCaptor.forClass(MonitoramentoInternet.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusInternet.OFFLINE);
        assertThat(captor.getValue().getMensagemErro()).contains("Connection refused");

        verify(alertService).registrarOuIgnorar(
                eq(TipoAlerta.INTERNET), eq(AlertService.ORIGEM_GLOBAL), eq(NivelAlerta.CRITICAL), anyString());
    }
}
