package br.com.monitoramento.auth.dto;

public class LoginResponse {

    private final String token;
    private final String tipo = "Bearer";
    private final String username;
    private final String role;

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
