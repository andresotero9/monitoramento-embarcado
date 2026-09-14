package br.com.monitoramento.monitoring.disk.repository;

import br.com.monitoramento.monitoring.disk.entity.MonitoramentoDisco;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonitoramentoDiscoRepository extends JpaRepository<MonitoramentoDisco, Long> {

    Page<MonitoramentoDisco> findAllByOrderByDataHoraDesc(Pageable pageable);

    Optional<MonitoramentoDisco> findFirstByOrderByDataHoraDesc();
}
