package br.com.monitoramento.auth.dto;

import java.time.OffsetDateTime;

/**
 * Dados de um usuário expostos pela API. Nunca inclui a senha/hash —
 * ver {@link br.com.monitoramento.auth.entity.Usuario#getPasswordHash()}.
 */
public class UsuarioResponse {

    private final Long id;
    private final String nome;
    private final String username;
    private final String role;
    private final boolean ativo;
    private final OffsetDateTime dataCriacao;

    public UsuarioResponse(Long id, String nome, String username, String role,
                            boolean ativo, OffsetDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.role = role;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }
}
