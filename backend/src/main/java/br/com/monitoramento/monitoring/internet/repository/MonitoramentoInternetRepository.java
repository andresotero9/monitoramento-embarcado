package br.com.monitoramento.monitoring.internet.repository;

import br.com.monitoramento.monitoring.internet.entity.MonitoramentoInternet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonitoramentoInternetRepository extends JpaRepository<MonitoramentoInternet, Long> {

    Page<MonitoramentoInternet> findAllByOrderByDataHoraDesc(Pageable pageable);

    Optional<MonitoramentoInternet> findFirstByOrderByDataHoraDesc();
}
