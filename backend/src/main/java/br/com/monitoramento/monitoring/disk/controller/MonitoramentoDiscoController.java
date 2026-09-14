package br.com.monitoramento.monitoring.disk.controller;

import br.com.monitoramento.monitoring.disk.dto.MonitoramentoDiscoDTO;
import br.com.monitoramento.monitoring.disk.service.DiskMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoramento/disco")
@Tag(name = "Monitoramento - Disco", description = "Histórico de uso de disco do filesystem Linux")
public class MonitoramentoDiscoController {

    private final DiskMonitoringService diskMonitoringService;

    public MonitoramentoDiscoController(DiskMonitoringService diskMonitoringService) {
        this.diskMonitoringService = diskMonitoringService;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de uso de disco, paginado")
    public ResponseEntity<Page<MonitoramentoDiscoDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(diskMonitoringService.listar(pageable));
    }

    @GetMapping("/latest")
    @Operation(summary = "Retorna a verificação de disco mais recente")
    public ResponseEntity<MonitoramentoDiscoDTO> buscarUltimo() {
        return ResponseEntity.ok(diskMonitoringService.buscarUltimo());
    }
}
