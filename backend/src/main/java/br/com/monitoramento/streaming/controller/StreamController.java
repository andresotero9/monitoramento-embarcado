package br.com.monitoramento.streaming.controller;

import br.com.monitoramento.exception.ResourceNotFoundException;
import br.com.monitoramento.streaming.StreamSessionManager;
import br.com.monitoramento.streaming.StreamStatus;
import br.com.monitoramento.streaming.dto.StreamStartResponseDTO;
import br.com.monitoramento.streaming.dto.StreamStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Expõe o streaming HLS sob demanda das câmeras.
 *
 * <p>{@code start}/{@code stop}/{@code status} exigem JWT normalmente. Já
 * {@code playlist.m3u8} e os segmentos {@code .ts} são endpoints públicos do ponto
 * de vista do Spring Security (a tag {@code <video>} não envia o header
 * {@code Authorization}), mas validam um token opaco de sessão via query string
 * (ver {@link StreamSessionManager}).</p>
 */
@RestController
@RequestMapping("/api/stream")
@Tag(name = "Streaming", description = "Conversão RTSP → HLS sob demanda para visualização das câmeras")
public class StreamController {

    private static final MediaType MEDIA_TYPE_HLS_PLAYLIST = MediaType.valueOf("application/vnd.apple.mpegurl");
    private static final MediaType MEDIA_TYPE_HLS_SEGMENT = MediaType.valueOf("video/mp2t");

    private final StreamSessionManager streamSessionManager;

    public StreamController(StreamSessionManager streamSessionManager) {
        this.streamSessionManager = streamSessionManager;
    }

    @PostMapping("/{cameraId}/start")
    @Operation(summary = "Inicia (ou reaproveita) a sessão de streaming HLS de uma câmera")
    public ResponseEntity<StreamStartResponseDTO> iniciar(@PathVariable Long cameraId) {
        String token = streamSessionManager.iniciar(cameraId);
        String playlistUrl = "/api/stream/%d/playlist.m3u8?token=%s".formatted(cameraId, token);
        return ResponseEntity.ok(new StreamStartResponseDTO(cameraId, playlistUrl));
    }

    @DeleteMapping("/{cameraId}/stop")
    @Operation(summary = "Encerra a sessão de streaming HLS de uma câmera")
    public ResponseEntity<Void> parar(@PathVariable Long cameraId) {
        streamSessionManager.parar(cameraId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cameraId}/status")
    @Operation(summary = "Consulta o status da sessão de streaming de uma câmera")
    public ResponseEntity<StreamStatusResponseDTO> status(@PathVariable Long cameraId) {
        StreamStatus status = streamSessionManager.status(cameraId);
        return ResponseEntity.ok(new StreamStatusResponseDTO(cameraId, status));
    }

    @GetMapping("/{cameraId}/playlist.m3u8")
    @Operation(summary = "Retorna a playlist HLS da câmera (autenticada via token de sessão)")
    public ResponseEntity<String> playlist(@PathVariable Long cameraId, @RequestParam String token) {
        validarToken(cameraId, token);

        Path arquivo = streamSessionManager.resolverArquivo(cameraId, "index.m3u8");
        if (arquivo == null || !Files.exists(arquivo)) {
            throw new ResourceNotFoundException(
                    "Playlist ainda não disponível para a câmera id=" + cameraId + " (status ainda INICIANDO)");
        }

        String conteudoOriginal = lerArquivoTexto(arquivo);
        String conteudoComToken = anexarTokenAosSegmentos(conteudoOriginal, token);

        return ResponseEntity.ok()
                .contentType(MEDIA_TYPE_HLS_PLAYLIST)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(conteudoComToken);
    }

    @GetMapping("/{cameraId}/{segmentName:segment_\\d+\\.ts}")
    @Operation(summary = "Retorna um segmento de vídeo HLS (autenticado via token de sessão)")
    public ResponseEntity<FileSystemResource> segmento(
            @PathVariable Long cameraId, @PathVariable String segmentName, @RequestParam String token) {
        validarToken(cameraId, token);

        Path arquivo = streamSessionManager.resolverArquivo(cameraId, segmentName);
        if (arquivo == null || !Files.exists(arquivo)) {
            throw new ResourceNotFoundException("Segmento não encontrado: " + segmentName);
        }

        return ResponseEntity.ok()
                .contentType(MEDIA_TYPE_HLS_SEGMENT)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(new FileSystemResource(arquivo));
    }

    private void validarToken(Long cameraId, String token) {
        if (!streamSessionManager.validarTokenERenovarAcesso(cameraId, token)) {
            throw new AccessDeniedException("Token de streaming inválido ou sessão expirada");
        }
    }

    /**
     * Reescreve cada linha de segmento da playlist anexando {@code ?token=...},
     * já que a resolução relativa de URLs do HLS não herdaria a query string da
     * própria playlist. Assim, quando o player buscar "segment_000.ts", a URL
     * resultante já contém o token necessário para autenticação.
     */
    private String anexarTokenAosSegmentos(String conteudoOriginal, String token) {
        StringBuilder resultado = new StringBuilder();
        for (String linha : conteudoOriginal.split("\n")) {
            if (linha.isBlank() || linha.startsWith("#")) {
                resultado.append(linha).append('\n');
            } else {
                resultado.append(linha.strip()).append("?token=").append(token).append('\n');
            }
        }
        return resultado.toString();
    }

    private String lerArquivoTexto(Path arquivo) {
        try {
            return Files.readString(arquivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler playlist HLS: " + arquivo, e);
        }
    }
}
