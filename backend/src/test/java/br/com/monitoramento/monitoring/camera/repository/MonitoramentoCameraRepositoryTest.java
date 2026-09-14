package br.com.monitoramento.monitoring.camera.repository;

import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.repository.CameraRepository;
import br.com.monitoramento.monitoring.camera.entity.MonitoramentoCamera;
import br.com.monitoramento.monitoring.camera.entity.StatusCamera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o CRUD e as consultas derivadas de {@link MonitoramentoCameraRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MonitoramentoCameraRepositoryTest {

    @Autowired
    private MonitoramentoCameraRepository monitoramentoCameraRepository;
    @Autowired
    private CameraRepository cameraRepository;

    private Camera camera;

    @BeforeEach
    void configurar() {
        monitoramentoCameraRepository.deleteAll();
        cameraRepository.deleteAll();
        camera = cameraRepository.save(
                new Camera("Entrada", "Descrição", "192.168.0.10", 80, 554, "admin", "senha", true));
    }

    private MonitoramentoCamera novoRegistro(OffsetDateTime dataHora, StatusCamera status) {
        return new MonitoramentoCamera(camera, dataHora, status, 20L, status == StatusCamera.ONLINE, null);
    }

    @Test
    void salvar_deveGerarIdEAssociarCamera() {
        MonitoramentoCamera salvo = monitoramentoCameraRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusCamera.ONLINE));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCamera().getId()).isEqualTo(camera.getId());
    }

    @Test
    void buscarPorId_deveRetornarRegistroExistente() {
        Long id = monitoramentoCameraRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusCamera.OFFLINE)).getId();

        Optional<MonitoramentoCamera> encontrado = monitoramentoCameraRepository.findById(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusCamera.OFFLINE);
    }

    @Test
    void listarTodos_deveRetornarTodosOsRegistrosSalvos() {
        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now().minusMinutes(5), StatusCamera.ONLINE));
        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now(), StatusCamera.OFFLINE));

        List<MonitoramentoCamera> todos = monitoramentoCameraRepository.findAll();

        assertThat(todos).hasSize(2);
    }

    @Test
    void excluir_deveRemoverRegistro() {
        Long id = monitoramentoCameraRepository.save(
                novoRegistro(OffsetDateTime.now(), StatusCamera.ONLINE)).getId();

        monitoramentoCameraRepository.deleteById(id);

        assertThat(monitoramentoCameraRepository.findById(id)).isEmpty();
    }

    @Test
    void findAllByCameraIdOrderByDataHoraDesc_deveRetornarApenasRegistrosDaCamera() {
        Camera outraCamera = cameraRepository.save(
                new Camera("Fundos", "Descrição", "192.168.0.20", 80, 554, "admin", "senha", true));

        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now().minusMinutes(10), StatusCamera.ONLINE));
        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now(), StatusCamera.OFFLINE));
        monitoramentoCameraRepository.save(
                new MonitoramentoCamera(outraCamera, OffsetDateTime.now(), StatusCamera.ONLINE, 10L, true, null));

        Page<MonitoramentoCamera> pagina = monitoramentoCameraRepository
                .findAllByCameraIdOrderByDataHoraDesc(camera.getId(), PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getContent().get(0).getStatus()).isEqualTo(StatusCamera.OFFLINE);
    }

    @Test
    void findFirstByCameraIdOrderByDataHoraDesc_deveRetornarRegistroMaisRecente() {
        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now().minusHours(1), StatusCamera.ONLINE));
        monitoramentoCameraRepository.save(novoRegistro(OffsetDateTime.now(), StatusCamera.OFFLINE));

        Optional<MonitoramentoCamera> maisRecente =
                monitoramentoCameraRepository.findFirstByCameraIdOrderByDataHoraDesc(camera.getId());

        assertThat(maisRecente).isPresent();
        assertThat(maisRecente.get().getStatus()).isEqualTo(StatusCamera.OFFLINE);
    }
}
