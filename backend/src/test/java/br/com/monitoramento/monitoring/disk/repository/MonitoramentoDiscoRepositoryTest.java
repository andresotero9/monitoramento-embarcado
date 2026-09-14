package br.com.monitoramento.monitoring.disk.repository;

import br.com.monitoramento.monitoring.disk.entity.MonitoramentoDisco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o CRUD e as consultas derivadas de {@link MonitoramentoDiscoRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MonitoramentoDiscoRepositoryTest {

    @Autowired
    private MonitoramentoDiscoRepository monitoramentoDiscoRepository;

    @BeforeEach
    void limpar() {
        monitoramentoDiscoRepository.deleteAll();
    }

    private MonitoramentoDisco novoRegistro(OffsetDateTime dataHora, BigDecimal percentual) {
        return new MonitoramentoDisco(100_000L, 60_000L, 40_000L, percentual, dataHora);
    }

    @Test
    void salvar_deveGerarId() {
        MonitoramentoDisco salvo = monitoramentoDiscoRepository.save(
                novoRegistro(OffsetDateTime.now(), new BigDecimal("60.00")));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getPercentualUtilizado()).isEqualByComparingTo("60.00");
    }

    @Test
    void buscarPorId_deveRetornarRegistroExistente() {
        Long id = monitoramentoDiscoRepository.save(
                novoRegistro(OffsetDateTime.now(), new BigDecimal("45.50"))).getId();

        Optional<MonitoramentoDisco> encontrado = monitoramentoDiscoRepository.findById(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEspacoLivre()).isEqualTo(40_000L);
    }

    @Test
    void listarTodos_deveRetornarTodosOsRegistrosSalvos() {
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now().minusHours(1), new BigDecimal("50.00")));
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now(), new BigDecimal("55.00")));

        List<MonitoramentoDisco> todos = monitoramentoDiscoRepository.findAll();

        assertThat(todos).hasSize(2);
    }

    @Test
    void excluir_deveRemoverRegistro() {
        Long id = monitoramentoDiscoRepository.save(
                novoRegistro(OffsetDateTime.now(), new BigDecimal("70.00"))).getId();

        monitoramentoDiscoRepository.deleteById(id);

        assertThat(monitoramentoDiscoRepository.findById(id)).isEmpty();
    }

    @Test
    void findAllByOrderByDataHoraDesc_deveRetornarOrdenadoDoMaisRecenteParaOMaisAntigo() {
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now().minusHours(2), new BigDecimal("40.00")));
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now(), new BigDecimal("65.00")));

        Page<MonitoramentoDisco> pagina =
                monitoramentoDiscoRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getContent().get(0).getPercentualUtilizado()).isEqualByComparingTo("65.00");
    }

    @Test
    void findFirstByOrderByDataHoraDesc_deveRetornarRegistroMaisRecente() {
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now().minusDays(1), new BigDecimal("30.00")));
        monitoramentoDiscoRepository.save(novoRegistro(OffsetDateTime.now(), new BigDecimal("80.00")));

        Optional<MonitoramentoDisco> maisRecente = monitoramentoDiscoRepository.findFirstByOrderByDataHoraDesc();

        assertThat(maisRecente).isPresent();
        assertThat(maisRecente.get().getPercentualUtilizado()).isEqualByComparingTo("80.00");
    }
}
