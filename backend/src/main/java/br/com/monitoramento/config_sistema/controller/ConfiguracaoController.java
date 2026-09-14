package br.com.monitoramento.config_sistema.controller;

import br.com.monitoramento.config_sistema.dto.ConfiguracaoDTO;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta e atualização das configurações de monitoramento
 * (Internet, Disco e Câmeras).
 */
@RestController
@RequestMapping("/api/configuracoes")
@Tag(name = "Configurações", description = "Parâmetros de monitoramento de Internet, Disco e Câmeras")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as configurações do sistema")
    public ResponseEntity<List<ConfiguracaoDTO>> listarTodas() {
        return ResponseEntity.ok(configuracaoService.listarTodas());
    }

    @PutMapping
    @Operation(summary = "Atualiza o valor de uma ou mais configurações, identificadas pela chave")
    public ResponseEntity<List<ConfiguracaoDTO>> atualizar(
            @Valid @RequestBody List<ConfiguracaoDTO> configuracoes) {
        return ResponseEntity.ok(configuracaoService.atualizar(configuracoes));
    }
}
