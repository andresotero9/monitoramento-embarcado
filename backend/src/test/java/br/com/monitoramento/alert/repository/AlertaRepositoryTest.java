package br.com.monitoramento.alert.repository;

import br.com.monitoramento.alert.entity.Alerta;
import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o CRUD e as consultas derivadas de {@link AlertaRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AlertaRepositoryTest {

    @Autowired
    private AlertaRepository alertaRepository;

    @BeforeEach
    void limpar() {
        alertaRepository.deleteAll();
    }

    @Test
    void salvar_deveGerarIdEPersistirCampos() {
        Alerta alerta = new Alerta(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "Espaço em disco baixo");

        Alerta salvo = alertaRepository.save(alerta);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTipo()).isEqualTo(TipoAlerta.DISCO);
        assertThat(salvo.getOrigem()).isEqualTo("GLOBAL");
        assertThat(salvo.getNivel()).isEqualTo(NivelAlerta.WARNING);
        assertThat(salvo.isResolvido()).isFalse();
    }

    @Test
    void buscarPorId_deveRetornarAlertaExistente() {
        Long id = alertaRepository.save(
                new Alerta(TipoAlerta.INTERNET, "GLOBAL", NivelAlerta.CRITICAL, "Sem conectividade")).getId();

        Optional<Alerta> encontrado = alertaRepository.findById(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMensagem()).isEqualTo("Sem conectividade");
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveRetornarVazio() {
        assertThat(alertaRepository.findById(999L)).isEmpty();
    }

    @Test
    void listarTodos_deveRetornarTodosOsAlertasSalvos() {
        alertaRepository.save(new Alerta(TipoAlerta.CAMERA, "1", NivelAlerta.INFO, "Câmera reconectada"));
        alertaRepository.save(new Alerta(TipoAlerta.SISTEMA, "GLOBAL", NivelAlerta.WARNING, "Uso de CPU alto"));

        List<Alerta> todos = alertaRepository.findAll();

        assertThat(todos).hasSize(2);
    }

    @Test
    void atualizar_deveResolverAlertaEPersistirAlteracao() {
        Alerta salvo = alertaRepository.save(
                new Alerta(TipoAlerta.CAMERA, "1", NivelAlerta.CRITICAL, "Câmera offline"));

        salvo.resolver();
        alertaRepository.save(salvo);

        Alerta recarregado = alertaRepository.findById(salvo.getId()).orElseThrow();
        assertThat(recarregado.isResolvido()).isTrue();
        assertThat(recarregado.getDataResolucao()).isNotNull();
    }

    @Test
    void excluir_deveRemoverAlerta() {
        Long id = alertaRepository.save(
                new Alerta(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.INFO, "Uso normalizado")).getId();

        alertaRepository.deleteById(id);

        assertThat(alertaRepository.findById(id)).isEmpty();
    }

    @Test
    void findAllByResolvidoFalseOrderByDataHoraDesc_deveRetornarApenasAbertos() {
        Alerta resolvido = alertaRepository.save(
                new Alerta(TipoAlerta.INTERNET, "GLOBAL", NivelAlerta.WARNING, "Instável"));
        resolvido.resolver();
        alertaRepository.save(resolvido);
        alertaRepository.save(new Alerta(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.CRITICAL, "Disco cheio"));

        List<Alerta> abertos = alertaRepository.findAllByResolvidoFalseOrderByDataHoraDesc();

        assertThat(abertos).hasSize(1);
        assertThat(abertos.get(0).getMensagem()).isEqualTo("Disco cheio");
    }

    @Test
    void findFirstByTipoAndOrigemAndResolvidoFalse_deveLocalizarAlertaAberto() {
        alertaRepository.save(new Alerta(TipoAlerta.CAMERA, "5", NivelAlerta.CRITICAL, "Câmera offline"));

        Optional<Alerta> encontrado =
                alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta.CAMERA, "5");

        assertThat(encontrado).isPresent();
    }
}
