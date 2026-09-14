package br.com.monitoramento.alert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Alerta gerado pelo sistema de monitoramento.
 *
 * <p>{@code origem} identifica a fonte do alerta: o id da câmera (como texto) para
 * {@link TipoAlerta#CAMERA}, ou {@code "GLOBAL"} para INTERNET/DISCO/SISTEMA.</p>
 *
 * <p>A combinação (tipo, origem) só pode ter um alerta ABERTO por vez — reforçado
 * também por um índice único parcial no banco (ver migration V7) — garantindo que
 * não sejam gerados alertas duplicados enquanto o problema persistir.</p>
 */
@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipo;

    @Column(nullable = false, length = 100)
    private String origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAlerta nivel;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Column(nullable = false)
    private boolean resolvido;

    @Column(name = "data_resolucao")
    private OffsetDateTime dataResolucao;

    protected Alerta() {
        // Construtor exigido pelo JPA
    }

    public Alerta(TipoAlerta tipo, String origem, NivelAlerta nivel, String mensagem) {
        this.tipo = tipo;
        this.origem = origem;
        this.nivel = nivel;
        this.mensagem = mensagem;
        this.dataHora = OffsetDateTime.now();
        this.resolvido = false;
    }

    public void resolver() {
        this.resolvido = true;
        this.dataResolucao = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public String getMensagem() {
        return mensagem;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public boolean isResolvido() {
        return resolvido;
    }

    public OffsetDateTime getDataResolucao() {
        return dataResolucao;
    }
}
