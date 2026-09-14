package br.com.monitoramento.alert.controller;

import br.com.monitoramento.alert.dto.AlertaDTO;
import br.com.monitoramento.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta e resolução de alertas gerados pelo sistema de monitoramento.
 */
@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Alertas de Internet, Disco, Câmeras e Sistema")
public class AlertaController {

    private final AlertService alertService;

    public AlertaController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os alertas, paginados, do mais recente para o mais antigo")
    public ResponseEntity<Page<AlertaDTO>> listarTodos(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(alertService.listarTodos(pageable));
    }

    @GetMapping("/recentes")
    @Operation(summary = "Lista os 10 alertas mais recentes")
    public ResponseEntity<List<AlertaDTO>> listarRecentes() {
        return ResponseEntity.ok(alertService.listarRecentes());
    }

    @GetMapping("/abertos")
    @Operation(summary = "Lista todos os alertas ainda não resolvidos")
    public ResponseEntity<List<AlertaDTO>> listarAbertos() {
        return ResponseEntity.ok(alertService.listarAbertos());
    }

    @PutMapping("/{id}/resolver")
    @Operation(summary = "Marca um alerta como resolvido")
    public ResponseEntity<AlertaDTO> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolverManualmente(id));
    }
}
