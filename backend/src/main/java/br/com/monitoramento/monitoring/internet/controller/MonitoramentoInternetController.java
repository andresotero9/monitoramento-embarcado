package br.com.monitoramento.monitoring.internet.controller;

import br.com.monitoramento.monitoring.internet.dto.MonitoramentoInternetDTO;
import br.com.monitoramento.monitoring.internet.service.InternetMonitoringService;
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
@RequestMapping("/api/monitoramento/internet")
@Tag(name = "Monitoramento - Internet", description = "Histórico de verificações de conectividade")
public class MonitoramentoInternetController {

    private final InternetMonitoringService internetMonitoringService;

    public MonitoramentoInternetController(InternetMonitoringService internetMonitoringService) {
        this.internetMonitoringService = internetMonitoringService;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de verificações de Internet, paginado")
    public ResponseEntity<Page<MonitoramentoInternetDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(internetMonitoringService.listar(pageable));
    }

    @GetMapping("/latest")
    @Operation(summary = "Retorna a verificação de Internet mais recente")
    public ResponseEntity<MonitoramentoInternetDTO> buscarUltimo() {
        return ResponseEntity.ok(internetMonitoringService.buscarUltimo());
    }
}
