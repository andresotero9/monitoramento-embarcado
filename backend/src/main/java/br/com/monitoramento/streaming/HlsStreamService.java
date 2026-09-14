package br.com.monitoramento.streaming;

import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.rtsp.RtspUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Monta e inicia o processo FFmpeg de conversão RTSP → HLS para uma câmera.
 *
 * <p>Usa {@code -c:v copy} (sem recodificar vídeo, assumindo H.264 — codec quase
 * universal em câmeras IP) para reduzir o uso de CPU do equipamento embarcado, e
 * {@code -an} (sem áudio) para evitar problemas de compatibilidade entre os
 * diversos codecs de áudio usados por diferentes fabricantes de câmera — áudio não
 * é um requisito deste sistema de monitoramento.</p>
 */
@Service
public class HlsStreamService {

    private static final String NOME_PLAYLIST = "index.m3u8";
    private static final String PADRAO_SEGMENTO = "segment_%03d.ts";
    private static final int HLS_TEMPO_SEGMENTO_SEGUNDOS = 2;
    private static final int HLS_QUANTIDADE_SEGMENTOS_PLAYLIST = 6;

    private final RtspUrlBuilder rtspUrlBuilder;
    private final String ffmpegPath;
    private final Path baseOutputDir;

    public HlsStreamService(RtspUrlBuilder rtspUrlBuilder,
                             @Value("${app.streaming.ffmpeg-path}") String ffmpegPath,
                             @Value("${app.streaming.hls-output-dir}") String hlsOutputDir) {
        this.rtspUrlBuilder = rtspUrlBuilder;
        this.ffmpegPath = ffmpegPath;
        this.baseOutputDir = Path.of(hlsOutputDir);
    }

    public Path resolverDiretorioSaida(Long cameraId) {
        return baseOutputDir.resolve(String.valueOf(cameraId));
    }

    public Path resolverArquivoPlaylist(Path diretorioSaida) {
        return diretorioSaida.resolve(NOME_PLAYLIST);
    }

    /**
     * Cria o diretório de saída e inicia o processo FFmpeg, que roda em background
     * gerando continuamente segmentos {@code .ts} e atualizando {@code index.m3u8}
     * até ser encerrado (ver {@code StreamSessionManager#parar}).
     */
    public Process iniciarProcesso(Camera camera, Path diretorioSaida) throws IOException {
        Files.createDirectories(diretorioSaida);
        String rtspUrl = rtspUrlBuilder.construir(camera);

        List<String> comando = List.of(
                ffmpegPath,
                "-y",
                "-rtsp_transport", "tcp",
                "-i", rtspUrl,
                "-an",
                "-c:v", "copy",
                "-f", "hls",
                "-hls_time", String.valueOf(HLS_TEMPO_SEGMENTO_SEGUNDOS),
                "-hls_list_size", String.valueOf(HLS_QUANTIDADE_SEGMENTOS_PLAYLIST),
                "-hls_flags", "delete_segments+append_list",
                "-hls_segment_filename", PADRAO_SEGMENTO,
                NOME_PLAYLIST
        );

        ProcessBuilder pb = new ProcessBuilder(comando);
        // Diretório de trabalho = diretório de saída: assim o FFmpeg escreve nomes de
        // arquivo relativos (ex.: "segment_000.ts"), e a playlist referencia apenas o
        // nome do arquivo, sem caminho absoluto — necessário para o rewrite de token
        // feito em StreamController.
        pb.directory(diretorioSaida.toFile());
        pb.redirectErrorStream(true);
        pb.redirectOutput(diretorioSaida.resolve("ffmpeg.log").toFile());

        return pb.start();
    }
}
