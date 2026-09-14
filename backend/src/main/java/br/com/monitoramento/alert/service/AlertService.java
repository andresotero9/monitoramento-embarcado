package br.com.monitoramento.alert.service;

import br.com.monitoramento.alert.dto.AlertaDTO;
import br.com.monitoramento.alert.entity.Alerta;
import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.repository.AlertaRepository;
import br.com.monitoramento.exception.BusinessException;
import br.com.monitoramento.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Centraliza a criação, deduplicação e resolução de alertas.
 *
 * <p>Regra de deduplicação: só pode existir um alerta ABERTO por par (tipo, origem)
 * simultaneamente. Enquanto o problema persistir, chamadas repetidas de
 * {@link #registrarOuIgnorar} não criam novos registros — apenas a primeira ocorrência
 * gera um alerta. Essa regra também é reforçada por um índice único parcial no banco
 * (migration V7), como defesa em profundidade.</p>
 */
@Service
public class AlertService {

    /**
     * Identificador de origem usado para alertas que não pertencem a uma câmera
     * específica (Internet, Disco, Sistema).
     */
    public static final String ORIGEM_GLOBAL = "GLOBAL";

    private final AlertaRepository alertaRepository;

    public AlertService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    /**
     * Cria um novo alerta para (tipo, origem) apenas se não houver um já aberto.
     * Caso já exista um alerta aberto, a chamada é ignorada (retorna o existente),
     * evitando duplicação enquanto o problema persistir.
     */
    @Transactional
    public Alerta registrarOuIgnorar(TipoAlerta tipo, String origem, NivelAlerta nivel, String mensagem) {
        return alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(tipo, origem)
                .orElseGet(() -> alertaRepository.save(new Alerta(tipo, origem, nivel, mensagem)));
    }

    /**
     * Resolve o alerta aberto para (tipo, origem), se existir — usado quando a
     * condição monitorada volta ao normal (ex.: disco abaixo do limite, câmera
     * online novamente). Não faz nada se não houver alerta aberto para essa origem.
     */
    @Transactional
    public Optional<Alerta> resolverAlertaAberto(TipoAlerta tipo, String origem) {
        return alertaRepository.findFirstByTipoAndOrigemAndResolvidoFalse(tipo, origem)
                .map(alerta -> {
                    alerta.resolver();
                    return alertaRepository.save(alerta);
                });
    }

    /**
     * Resolução manual de um alerta específico via API ({@code PUT /api/alertas/{id}/resolver}).
     */
    @Transactional
    public AlertaDTO resolverManualmente(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: id=" + id));

        if (alerta.isResolvido()) {
            throw new BusinessException("Alerta já está resolvido: id=" + id);
        }

        alerta.resolver();
        return toDTO(alertaRepository.save(alerta));
    }

    @Transactional(readOnly = true)
    public Page<AlertaDTO> listarTodos(Pageable pageable) {
        return alertaRepository.findAllByOrderByDataHoraDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> listarRecentes() {
        return alertaRepository.findTop10ByOrderByDataHoraDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> listarAbertos() {
        return alertaRepository.findAllByResolvidoFalseOrderByDataHoraDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    private AlertaDTO toDTO(Alerta alerta) {
        return new AlertaDTO(
                alerta.getId(), alerta.getTipo(), alerta.getOrigem(), alerta.getNivel(),
                alerta.getMensagem(), alerta.getDataHora(), alerta.isResolvido(), alerta.getDataResolucao());
    }
}
