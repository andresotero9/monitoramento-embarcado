package br.com.monitoramento.auth.controller;

import br.com.monitoramento.auth.dto.LoginRequest;
import br.com.monitoramento.auth.dto.LoginResponse;
import br.com.monitoramento.auth.dto.UsuarioCreateRequest;
import br.com.monitoramento.auth.dto.UsuarioResponse;
import br.com.monitoramento.auth.entity.Usuario;
import br.com.monitoramento.auth.repository.UsuarioRepository;
import br.com.monitoramento.auth.service.UsuarioService;
import br.com.monitoramento.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticação: login (emissão de JWT) e cadastro público de
 * novos usuários. O único usuário {@code ADMIN} do sistema é o administrador
 * padrão criado pela migration Flyway de dados iniciais — o cadastro público
 * sempre cria usuários com role {@code OPERADOR}.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, cadastro de usuários e emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                           UsuarioRepository usuarioRepository, JwtService jwtService,
                           UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Tenta autenticar o usuário com as credenciais fornecidas. Se falhar, lança BadCredentialsException, que será tratada pelo GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Se a autenticação foi bem-sucedida, busca o usuário no banco para gerar o token JWT.
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário autenticado não encontrado — estado inconsistente"));

        String token = jwtService.gerarToken(usuario.getUsername(), usuario.getRole().name());

        return ResponseEntity.ok(new LoginResponse(token, usuario.getUsername(), usuario.getRole().name()));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastra um novo usuário (role sempre OPERADOR, definida pelo backend)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (campos obrigatórios, senha fraca, senhas não conferem)"),
            @ApiResponse(responseCode = "409", description = "Username já cadastrado")
    })
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse criado = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
