package br.com.monitoramento.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para o cadastro público de um novo usuário.
 *
 * <p>A confirmação de senha é validada aqui mesmo, no DTO — {@code confirmPassword}
 * nunca é repassado à camada de serviço/negócio.</p>
 */
public class UsuarioCreateRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
    private String nome;

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 50, message = "O username deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "\\S+", message = "O username não pode conter espaços")
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 100, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "A senha deve conter letras e números"
    )
    private String password;

    @NotBlank(message = "A confirmação de senha é obrigatória")
    private String confirmPassword;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @AssertTrue(message = "As senhas não conferem")
    private boolean isSenhasConferem() {
        return password != null && password.equals(confirmPassword);
    }
}
