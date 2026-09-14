package br.com.monitoramento.config_sistema.dto;

import br.com.monitoramento.config_sistema.entity.TipoConfiguracao;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

/**
 * Representa uma configuração do sistema para leitura e atualização via API.
 */
public class ConfiguracaoDTO {

    private Long id;

    @NotBlank(message = "A chave da configuração é obrigatória")
    private String chave;

    @NotBlank(message = "O valor da configuração é obrigatório")
    private String valor;

    private TipoConfiguracao tipo;

    private String descricao;

    private OffsetDateTime dataAtualizacao;

    public ConfiguracaoDTO() {
    }

    public ConfiguracaoDTO(Long id, String chave, String valor, TipoConfiguracao tipo,
                            String descricao, OffsetDateTime dataAtualizacao) {
        this.id = id;
        this.chave = chave;
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public TipoConfiguracao getTipo() {
        return tipo;
    }

    public void setTipo(TipoConfiguracao tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public OffsetDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(OffsetDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
