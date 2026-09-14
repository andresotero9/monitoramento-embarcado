package br.com.monitoramento.monitoring.camera.controller;

import br.com.monitoramento.monitoring.camera.dto.MonitoramentoCameraDTO;
import br.com.monitoramento.monitoring.camera.service.CameraMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoramento/cameras")
@Tag(name = "Monitoramento - Câmeras", description = "Histórico de verificações de câmeras (ping + RTSP + frame)")
public class MonitoramentoCameraController {

    private final CameraMonitoringService cameraMonitoringService;

    public MonitoramentoCameraController(CameraMonitoringService cameraMonitoringService) {
        this.cameraMonitoringService = cameraMonitoringService;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de verificações de todas as câmeras, paginado")
    public ResponseEntity<Page<MonitoramentoCameraDTO>> listarTodas(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(cameraMonitoringService.listarTodas(pageable));
    }

    @GetMapping("/{cameraId}")
    @Operation(summary = "Lista o histórico de verificações de uma câmera específica, paginado")
    public ResponseEntity<Page<MonitoramentoCameraDTO>> listarPorCamera(
            @PathVariable Long cameraId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(cameraMonitoringService.listarPorCamera(cameraId, pageable));
    }

    @GetMapping("/{cameraId}/latest")
    @Operation(summary = "Retorna a verificação mais recente de uma câmera específica")
    public ResponseEntity<MonitoramentoCameraDTO> buscarUltimo(@PathVariable Long cameraId) {
        return ResponseEntity.ok(cameraMonitoringService.buscarUltimoPorCamera(cameraId));
    }
}
