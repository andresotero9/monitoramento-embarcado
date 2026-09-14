package br.com.monitoramento.camera.controller;

import br.com.monitoramento.camera.dto.CameraRequestDTO;
import br.com.monitoramento.camera.dto.CameraResponseDTO;
import br.com.monitoramento.camera.service.CameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD de câmeras IP. A senha nunca é retornada nas respostas
 * (ver {@link CameraResponseDTO}).
 */
@RestController
@RequestMapping("/api/cameras")
@Tag(name = "Câmeras", description = "Cadastro de câmeras IP monitoradas")
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as câmeras cadastradas")
    public ResponseEntity<List<CameraResponseDTO>> listarTodas() {
        return ResponseEntity.ok(cameraService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma câmera pelo id")
    public ResponseEntity<CameraResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cameraService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova câmera")
    public ResponseEntity<CameraResponseDTO> criar(@Valid @RequestBody CameraRequestDTO dto) {
        CameraResponseDTO criada = cameraService.criar(dto);
        return ResponseEntity.status(201).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma câmera existente")
    public ResponseEntity<CameraResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody CameraRequestDTO dto) {
        return ResponseEntity.ok(cameraService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma câmera cadastrada")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        cameraService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
