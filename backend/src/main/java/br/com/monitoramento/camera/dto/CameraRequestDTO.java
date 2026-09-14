package br.com.monitoramento.camera.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para criação ou atualização de uma câmera.
 *
 * <p>A senha é opcional em atualizações (PUT): quando não informada, a senha
 * atualmente armazenada é preservada.</p>
 */
public class CameraRequestDTO {

    @NotBlank(message = "O nome da câmera é obrigatório")
    @Size(max = 100)
    private String nome;

    @Size(max = 255)
    private String descricao;

    @NotBlank(message = "O endereço IP é obrigatório")
    @Size(max = 45)
    private String enderecoIp;

    @Min(1)
    @Max(65535)
    private Integer portaHttp;

    @NotNull(message = "A porta RTSP é obrigatória")
    @Min(1)
    @Max(65535)
    private Integer portaRtsp;

    @Size(max = 100)
    private String usuario;

    private String senha;

    @NotNull(message = "O campo 'ativa' é obrigatório")
    private Boolean ativa;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public void setEnderecoIp(String enderecoIp) {
        this.enderecoIp = enderecoIp;
    }

    public Integer getPortaHttp() {
        return portaHttp;
    }

    public void setPortaHttp(Integer portaHttp) {
        this.portaHttp = portaHttp;
    }

    public Integer getPortaRtsp() {
        return portaRtsp;
    }

    public void setPortaRtsp(Integer portaRtsp) {
        this.portaRtsp = portaRtsp;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}
