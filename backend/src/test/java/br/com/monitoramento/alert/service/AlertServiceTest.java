package br.com.monitoramento.alert.service;

import br.com.monitoramento.alert.entity.Alerta;
import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.repository.AlertaRepository;
import br.com.monitoramento.exception.BusinessException;
import br.com.monitoramento.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Cobre os cenários da Seção 17: criar alerta, evitar duplicação,
 * resolver alerta e criar recuperação.
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    private AlertService alertService;

    @BeforeEach
    void configurar() {
        alertService = new AlertService(alertaRepository);
    }

    @Test
    void registrarOuIgnorar_semAlertaAbertoExistente_deveCriarNovoAlerta() {
        when(alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta.DISCO, "GLOBAL"))
                .thenReturn(Optional.empty());
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta resultado = alertService.registrarOuIgnorar(
                TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "Uso de disco alto");

        assertThat(resultado.getTipo()).isEqualTo(TipoAlerta.DISCO);
        assertThat(resultado.isResolvido()).isFalse();
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void registrarOuIgnorar_comAlertaJaAberto_naoDeveCriarNovoENaoDuplicar() {
        Alerta existente = new Alerta(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "Uso de disco alto");
        when(alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta.DISCO, "GLOBAL"))
                .thenReturn(Optional.of(existente));

        Alerta primeira = alertService.registrarOuIgnorar(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "msg 1");
        Alerta segunda = alertService.registrarOuIgnorar(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "msg 2");

        assertThat(primeira).isSameAs(existente);
        assertThat(segunda).isSameAs(existente);
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void resolverSeExistirAberto_comAlertaAberto_deveResolverERegistrarRecuperacao() {
        Alerta aberto = new Alerta(TipoAlerta.CAMERA, "5", NivelAlerta.CRITICAL, "Câmera offline");
        when(alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta.CAMERA, "5"))
                .thenReturn(Optional.of(aberto));
        when(alertaRepository.save(aberto)).thenReturn(aberto);

        Optional<Alerta> resultado = alertService.resolverAlertaAberto(TipoAlerta.CAMERA, "5");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().isResolvido()).isTrue();
        assertThat(resultado.get().getDataResolucao()).isNotNull();
        verify(alertaRepository).save(aberto);
    }

    @Test
    void resolverSeExistirAberto_semAlertaAberto_naoDeveFazerNada() {
        when(alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta.CAMERA, "5"))
                .thenReturn(Optional.empty());

        Optional<Alerta> resultado = alertService.resolverAlertaAberto(TipoAlerta.CAMERA, "5");

        assertThat(resultado).isEmpty();
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void resolverManualmente_alertaAberto_deveResolver() {
        Alerta aberto = new Alerta(TipoAlerta.SISTEMA, "GLOBAL", NivelAlerta.INFO, "Teste");
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(aberto));
        when(alertaRepository.save(aberto)).thenReturn(aberto);

        var dto = alertService.resolverManualmente(1L);

        assertThat(dto.isResolvido()).isTrue();
    }

    @Test
    void resolverManualmente_alertaJaResolvido_deveLancarBusinessException() {
        Alerta jaResolvido = new Alerta(TipoAlerta.SISTEMA, "GLOBAL", NivelAlerta.INFO, "Teste");
        jaResolvido.resolver();
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(jaResolvido));

        assertThatThrownBy(() -> alertService.resolverManualmente(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolverManualmente_alertaInexistente_deveLancarResourceNotFoundException() {
        when(alertaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.resolverManualmente(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
