package br.com.monitoramento.monitoring.internet.repository;

import br.com.monitoramento.monitoring.internet.entity.MonitoramentoInternet;
import br.com.monitoramento.monitoring.internet.entity.StatusInternet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o CRUD e as consultas derivadas de {@link MonitoramentoInternetRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MonitoramentoInternetRepositoryTest {

    @Autowired
    private MonitoramentoInternetRepository monitoramentoInternetRepository;

    @BeforeEach
    void limpar() {
        monitoramentoInternetRepository.deleteAll();
    }

    private MonitoramentoInternet novoRegistro(OffsetDateTime dataHora, StatusInternet status) {
        return new MonitoramentoInternet(dataHora, status, status == StatusInternet.ONLINE ? 30L : null,
                status == StatusInternet.OFFLINE ? "Timeout ao pingar host" : null);
    }

    @Test
    void testSalvar() {
        MonitoramentoInternet salvo = monitoramentoInternetRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusInternet.ONLINE));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusInternet.ONLINE);
    }

    @Test
    void testBuscar() {
        Long id = monitoramentoInternetRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusInternet.OFFLINE)).getId();

        Optional<MonitoramentoInternet> encontrado = monitoramentoInternetRepository.findById(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMensagemErro()).isEqualTo("Timeout ao pingar host");
    }

    @Test
    void testListarTodos() {
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now().minusMinutes(30), StatusInternet.ONLINE));
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now(), StatusInternet.OFFLINE));

        List<MonitoramentoInternet> todos = monitoramentoInternetRepository.findAll();

        assertThat(todos).hasSize(2);
    }

    @Test
    void testDelete() {
        Long id = monitoramentoInternetRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusInternet.ONLINE)).getId();

        monitoramentoInternetRepository.deleteById(id);

        assertThat(monitoramentoInternetRepository.findById(id)).isEmpty();
    }

    @Test
    void testFindAllByOrderByDataHoraDesc() {
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now().minusHours(3), StatusInternet.ONLINE));
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now(), StatusInternet.OFFLINE));

        Page<MonitoramentoInternet> pagina =
                monitoramentoInternetRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getContent().get(0).getStatus()).isEqualTo(StatusInternet.OFFLINE);
    }

    @Test
    void findFirstByOrderByDataHoraDesc_deveRetornarRegistroMaisRecente() {
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now().minusDays(1), StatusInternet.ONLINE));
        monitoramentoInternetRepository.save(novoRegistro(OffsetDateTime.now(), StatusInternet.OFFLINE));

        Optional<MonitoramentoInternet> maisRecente = monitoramentoInternetRepository.findFirstByOrderByDataHoraDesc();

        assertThat(maisRecente).isPresent();
        assertThat(maisRecente.get().getStatus()).isEqualTo(StatusInternet.OFFLINE);
    }
}
