package br.com.monitoramento.auth.service;

import br.com.monitoramento.auth.dto.UsuarioCreateRequest;
import br.com.monitoramento.auth.dto.UsuarioResponse;
import br.com.monitoramento.auth.entity.Role;
import br.com.monitoramento.auth.entity.Usuario;
import br.com.monitoramento.auth.repository.UsuarioRepository;
import br.com.monitoramento.exception.UsernameJaCadastradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio para o cadastro de usuários.
 *
 * <p>Usuários cadastrados por aqui recebem sempre a role {@link Role#OPERADOR} —
 * a role nunca é escolhida pelo cliente. O único usuário {@link Role#ADMIN} do
 * sistema é o criado pela migration Flyway de dados iniciais.</p>
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new UsernameJaCadastradoException("Username já cadastrado.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(request.getNome(), request.getUsername(), passwordHash, Role.OPERADOR);

        Usuario salvo;
        try {
            salvo = usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            // Corrida entre a checagem acima e o INSERT: a constraint UNIQUE do banco
            // é a garantia definitiva contra usernames duplicados.
            throw new UsernameJaCadastradoException("Username já cadastrado.");
        }

        return new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getUsername(),
                salvo.getRole().name(), salvo.isAtivo(), salvo.getDataCriacao());
    }
}
