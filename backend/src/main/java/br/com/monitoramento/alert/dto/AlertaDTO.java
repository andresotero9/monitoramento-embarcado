package br.com.monitoramento.alert.dto;

import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;

import java.time.OffsetDateTime;

public class AlertaDTO {

    private Long id;
    private TipoAlerta tipo;
    private String origem;
    private NivelAlerta nivel;
    private String mensagem;
    private OffsetDateTime dataHora;
    private boolean resolvido;
    private OffsetDateTime dataResolucao;

    public AlertaDTO(Long id, TipoAlerta tipo, String origem, NivelAlerta nivel, String mensagem,
                      OffsetDateTime dataHora, boolean resolvido, OffsetDateTime dataResolucao) {
        this.id = id;
        this.tipo = tipo;
        this.origem = origem;
        this.nivel = nivel;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
        this.resolvido = resolvido;
        this.dataResolucao = dataResolucao;
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
