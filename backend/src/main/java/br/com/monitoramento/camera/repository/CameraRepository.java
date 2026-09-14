package br.com.monitoramento.camera.repository;

import br.com.monitoramento.camera.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CameraRepository extends JpaRepository<Camera, Long> {

    List<Camera> findByAtivaTrue();
}
