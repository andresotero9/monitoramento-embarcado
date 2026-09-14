package br.com.monitoramento.monitoring.camera.repository;

import br.com.monitoramento.monitoring.camera.entity.MonitoramentoCamera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonitoramentoCameraRepository extends JpaRepository<MonitoramentoCamera, Long> {

    Page<MonitoramentoCamera> findAllByCameraIdOrderByDataHoraDesc(Long cameraId, Pageable pageable);

    Optional<MonitoramentoCamera> findFirstByCameraIdOrderByDataHoraDesc(Long cameraId);

    Page<MonitoramentoCamera> findAllByOrderByDataHoraDesc(Pageable pageable);
}
