package br.com.monitoramento.auth.repository;

import br.com.monitoramento.auth.entity.Role;
import br.com.monitoramento.auth.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testa o CRUD e as consultas derivadas de {@link UsuarioRepository} contra um banco H2 real (perfil "test").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpar() {
        usuarioRepository.deleteAll();
    }

    @Test
    void salvar_deveGerarIdEPreencherDatasAutomaticamente() {
        Usuario usuario = new Usuario("Operador Teste", "operador.teste", "hash-bcrypt", Role.OPERADOR);

        Usuario salvo = usuarioRepository.save(usuario);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.isAtivo()).isTrue();
        assertThat(salvo.getDataCriacao()).isNotNull();
        assertThat(salvo.getDataAtualizacao()).isNotNull();
    }

    @Test
    void buscarPorId_deveRetornarUsuarioExistente() {
        Long id = usuarioRepository.save(new Usuario("Admin", "admin", "hash", Role.ADMIN)).getId();

        Optional<Usuario> encontrado = usuarioRepository.findById(id);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void listarTodos_deveRetornarTodosOsUsuariosSalvos() {
        usuarioRepository.save(new Usuario("Usuario Um", "usuario1", "hash1", Role.OPERADOR));
        usuarioRepository.save(new Usuario("Usuario Dois", "usuario2", "hash2", Role.ADMIN));

        List<Usuario> todos = usuarioRepository.findAll();

        assertThat(todos).hasSize(2);
    }

    @Test
    void atualizar_devePersistirAlteracaoEAtualizarDataAtualizacao() {
        Usuario salvo = usuarioRepository.save(new Usuario("Operador", "operador", "hash-antigo", Role.OPERADOR));

        salvo.setPasswordHash("hash-novo");
        salvo.setAtivo(false);
        usuarioRepository.saveAndFlush(salvo);

        Usuario recarregado = usuarioRepository.findById(salvo.getId()).orElseThrow();
        assertThat(recarregado.getPasswordHash()).isEqualTo("hash-novo");
        assertThat(recarregado.isAtivo()).isFalse();
    }

    @Test
    void excluir_deveRemoverUsuario() {
        Long id = usuarioRepository.save(new Usuario("Descartavel", "descartavel", "hash", Role.OPERADOR)).getId();

        usuarioRepository.deleteById(id);

        assertThat(usuarioRepository.findById(id)).isEmpty();
    }

    @Test
    void findByUsername_deveLocalizarUsuarioPorUsername() {
        usuarioRepository.save(new Usuario("Unico Usuario", "unico.usuario", "hash", Role.ADMIN));

        Optional<Usuario> encontrado = usuarioRepository.findByUsername("unico.usuario");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void findByUsername_quandoNaoExiste_deveRetornarVazio() {
        assertThat(usuarioRepository.findByUsername("inexistente")).isEmpty();
    }

    @Test
    void existsByUsername_quandoExiste_deveRetornarTrue() {
        usuarioRepository.save(new Usuario("Existente", "existente", "hash", Role.OPERADOR));

        assertThat(usuarioRepository.existsByUsername("existente")).isTrue();
    }

    @Test
    void existsByUsername_quandoNaoExiste_deveRetornarFalse() {
        assertThat(usuarioRepository.existsByUsername("inexistente")).isFalse();
    }

    @Test
    void salvar_comUsernameDuplicado_deveViolarRestricaoDeUnicidade() {
        usuarioRepository.saveAndFlush(new Usuario("Duplicado", "duplicado", "hash1", Role.OPERADOR));

        assertThatThrownBy(() ->
                usuarioRepository.saveAndFlush(new Usuario("Duplicado", "duplicado", "hash2", Role.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
