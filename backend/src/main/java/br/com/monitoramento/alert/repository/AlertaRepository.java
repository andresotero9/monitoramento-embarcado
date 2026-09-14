package br.com.monitoramento.alert.repository;

import br.com.monitoramento.alert.entity.Alerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    Page<Alerta> findAllByOrderByDataHoraDesc(Pageable pageable);

    List<Alerta> findTop10ByOrderByDataHoraDesc();

    List<Alerta> findAllByResolvidoFalseOrderByDataHoraDesc();

    /**
     * Localiza um alerta aberto para o par (tipo, origem), usado tanto para
     * evitar duplicação quanto para localizar o alerta a ser resolvido quando
     * a condição volta ao normal.
     */
    Optional<Alerta> findFirstByTipoAndOrigemAndResolvidoFalse(TipoAlerta tipo, String origem);
}
