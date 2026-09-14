package br.com.monitoramento.config_sistema.repository;

import br.com.monitoramento.config_sistema.entity.Configuracao;
import br.com.monitoramento.config_sistema.entity.TipoConfiguracao;
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
 * Testa o CRUD e as consultas derivadas de {@link ConfiguracaoRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ConfiguracaoRepositoryTest {

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    @BeforeEach
    void limpar() {
        configuracaoRepository.deleteAll();
    }

    @Test
    void salvar_deveGerarId() {
        Configuracao salva = configuracaoRepository.save(
                new Configuracao("alerta.disco.limite", "90", TipoConfiguracao.INTEGER, "Limite de uso de disco"));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getChave()).isEqualTo("alerta.disco.limite");
    }

    @Test
    void buscarPorId_deveRetornarConfiguracaoExistente() {
        Long id = configuracaoRepository.save(
                new Configuracao("monitoramento.intervalo", "60", TipoConfiguracao.INTEGER, "Intervalo em segundos"))
                .getId();

        Optional<Configuracao> encontrada = configuracaoRepository.findById(id);

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getValor()).isEqualTo("60");
    }

    @Test
    void listarTodas_deveRetornarTodasAsConfiguracoesSalvas() {
        configuracaoRepository.save(new Configuracao("chave1", "valor1", TipoConfiguracao.STRING, null));
        configuracaoRepository.save(new Configuracao("chave2", "valor2", TipoConfiguracao.STRING, null));

        List<Configuracao> todas = configuracaoRepository.findAll();

        assertThat(todas).hasSize(2);
    }

    @Test
    void atualizar_devePersistirNovoValor() {
        Configuracao salva = configuracaoRepository.save(
                new Configuracao("alerta.internet.timeout", "5000", TipoConfiguracao.INTEGER, null));

        salva.setValor("8000");
        configuracaoRepository.saveAndFlush(salva);

        Configuracao recarregada = configuracaoRepository.findById(salva.getId()).orElseThrow();
        assertThat(recarregada.getValor()).isEqualTo("8000");
    }

    @Test
    void excluir_deveRemoverConfiguracao() {
        Long id = configuracaoRepository.save(
                new Configuracao("descartavel", "valor", TipoConfiguracao.STRING, null)).getId();

        configuracaoRepository.deleteById(id);

        assertThat(configuracaoRepository.findById(id)).isEmpty();
    }

    @Test
    void findByChave_deveLocalizarConfiguracaoPorChave() {
        configuracaoRepository.save(
                new Configuracao("notificacao.email.ativo", "true", TipoConfiguracao.BOOLEAN, null));

        Optional<Configuracao> encontrada = configuracaoRepository.findByChave("notificacao.email.ativo");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTipo()).isEqualTo(TipoConfiguracao.BOOLEAN);
    }

    @Test
    void findByChave_quandoNaoExiste_deveRetornarVazio() {
        assertThat(configuracaoRepository.findByChave("inexistente")).isEmpty();
    }
}
