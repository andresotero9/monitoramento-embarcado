package br.com.monitoramento.config_sistema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Configuração do sistema armazenada no modelo chave/valor tipado.
 *
 * <p>Esse modelo evita novas migrations a cada novo parâmetro de monitoramento
 * (ver decisão técnica registrada no README).</p>
 */
@Entity
@Table(name = "configuracao")
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String chave;

    @Column(nullable = false, length = 500)
    private String valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConfiguracao tipo;

    @Column(length = 255)
    private String descricao;

    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

    protected Configuracao() {
        // Construtor exigido pelo JPA
    }

    public Configuracao(String chave, String valor, TipoConfiguracao tipo, String descricao) {
        this.chave = chave;
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    @PrePersist
    protected void aoCriar() {
        this.dataAtualizacao = OffsetDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
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

    public String getDescricao() {
        return descricao;
    }

    public OffsetDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
