package br.com.monitoramento.camera.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Câmera IP cadastrada para monitoramento via RTSP.
 *
 * <p>A senha da câmera é armazenada cifrada em {@link #senhaCriptografada}
 * (AES, cifra/decifra feita em {@code CameraCredentialCipher}, camada de serviço),
 * e nunca é exposta pela API — ver {@code CameraResponseDTO}.</p>
 */
@Entity
@Table(name = "camera")
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "endereco_ip", nullable = false, length = 45)
    private String enderecoIp;

    @Column(name = "porta_http")
    private Integer portaHttp;

    @Column(name = "porta_rtsp", nullable = false)
    private Integer portaRtsp = 554;

    @Column(length = 100)
    private String usuario;

    @Column(name = "senha_criptografada", length = 500)
    private String senhaCriptografada;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

    protected Camera() {
        // Construtor exigido pelo JPA
    }

    public Camera(String nome, String descricao, String enderecoIp, Integer portaHttp,
                  Integer portaRtsp, String usuario, String senhaCriptografada, boolean ativa) {
        this.nome = nome;
        this.descricao = descricao;
        this.enderecoIp = enderecoIp;
        this.portaHttp = portaHttp;
        this.portaRtsp = portaRtsp != null ? portaRtsp : 554;
        this.usuario = usuario;
        this.senhaCriptografada = senhaCriptografada;
        this.ativa = ativa;
    }

    @PrePersist
    protected void aoCriar() {
        OffsetDateTime agora = OffsetDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = OffsetDateTime.now();
    }

    public void atualizarDados(String nome, String descricao, String enderecoIp, Integer portaHttp,
                                Integer portaRtsp, String usuario, String senhaCriptografada, boolean ativa) {
        this.nome = nome;
        this.descricao = descricao;
        this.enderecoIp = enderecoIp;
        this.portaHttp = portaHttp;
        this.portaRtsp = portaRtsp != null ? portaRtsp : this.portaRtsp;
        this.usuario = usuario;
        if (senhaCriptografada != null && !senhaCriptografada.isBlank()) {
            this.senhaCriptografada = senhaCriptografada;
        }
        this.ativa = ativa;
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

    public String getSenhaCriptografada() {
        return senhaCriptografada;
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
