package br.com.monitoramento.camera.repository;

import br.com.monitoramento.camera.entity.Camera;
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
 * Testa o CRUD e as consultas derivadas de {@link CameraRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CameraRepositoryTest {

    @Autowired
    private CameraRepository cameraRepository;

    @BeforeEach
    void limpar() {
        cameraRepository.deleteAll();
    }

    private Camera novaCamera(String nome, boolean ativa) {
        return new Camera(nome, "Descrição de " + nome, "192.168.0.10", 80, 554,
                "admin", "senha-cifrada", ativa);
    }

    @Test
    void salvar_deveGerarIdEPreencherDatasAutomaticamente() {
        Camera salva = cameraRepository.save(novaCamera("Entrada", true));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getDataCriacao()).isNotNull();
        assertThat(salva.getDataAtualizacao()).isNotNull();
    }

    @Test
    void buscarPorId_deveRetornarCameraExistente() {
        Long id = cameraRepository.save(novaCamera("Recepção", true)).getId();

        Optional<Camera> encontrada = cameraRepository.findById(id);

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Recepção");
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveRetornarVazio() {
        assertThat(cameraRepository.findById(999L)).isEmpty();
    }

    @Test
    void listarTodas_deveRetornarTodasAsCamerasSalvas() {
        cameraRepository.save(novaCamera("Câmera 1", true));
        cameraRepository.save(novaCamera("Câmera 2", false));

        List<Camera> todas = cameraRepository.findAll();

        assertThat(todas).hasSize(2);
    }

    @Test
    void atualizar_devePersistirAlteracaoDosDados() {
        Camera salva = cameraRepository.save(novaCamera("Original", true));

        salva.atualizarDados("Atualizada", "Nova descrição", "192.168.0.20", 8080, 555,
                "novo-usuario", null, false);
        cameraRepository.saveAndFlush(salva);

        Camera recarregada = cameraRepository.findById(salva.getId()).orElseThrow();
        assertThat(recarregada.getNome()).isEqualTo("Atualizada");
        assertThat(recarregada.getEnderecoIp()).isEqualTo("192.168.0.20");
        assertThat(recarregada.isAtiva()).isFalse();
        // senha não enviada na atualização deve ser preservada
        assertThat(recarregada.getSenhaCriptografada()).isEqualTo("senha-cifrada");
    }

    @Test
    void excluir_deveRemoverCamera() {
        Long id = cameraRepository.save(novaCamera("Descartável", true)).getId();

        cameraRepository.deleteById(id);

        assertThat(cameraRepository.findById(id)).isEmpty();
    }

    @Test
    void findByAtivaTrue_deveRetornarApenasCamerasAtivas() {
        cameraRepository.save(novaCamera("Ativa", true));
        cameraRepository.save(novaCamera("Inativa", false));

        List<Camera> ativas = cameraRepository.findByAtivaTrue();

        assertThat(ativas).hasSize(1);
        assertThat(ativas.get(0).getNome()).isEqualTo("Ativa");
    }
}
