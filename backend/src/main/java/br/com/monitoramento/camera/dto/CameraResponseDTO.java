package br.com.monitoramento.camera.dto;

import java.time.OffsetDateTime;

/**
 * Representação de câmera retornada pela API. Intencionalmente NÃO possui
 * campo de senha — a senha da câmera nunca é exposta pela API.
 */
public class CameraResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String enderecoIp;
    private Integer portaHttp;
    private Integer portaRtsp;
    private String usuario;
    private boolean ativa;
    private OffsetDateTime dataCriacao;
    private OffsetDateTime dataAtualizacao;

    public CameraResponseDTO(Long id, String nome, String descricao, String enderecoIp,
                              Integer portaHttp, Integer portaRtsp, String usuario, boolean ativa,
                              OffsetDateTime dataCriacao, OffsetDateTime dataAtualizacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.enderecoIp = enderecoIp;
        this.portaHttp = portaHttp;
        this.portaRtsp = portaRtsp;
        this.usuario = usuario;
        this.ativa = ativa;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public Integer getPortaHttp() {
        return portaHttp;
    }

    public Integer getPortaRtsp() {
        return portaRtsp;
    }

    public String getUsuario() {
        return usuario;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public OffsetDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
