package br.com.monitoramento.streaming.dto;

public class StreamStartResponseDTO {

    private final Long cameraId;
    private final String playlistUrl;

    public StreamStartResponseDTO(Long cameraId, String playlistUrl) {
        this.cameraId = cameraId;
        this.playlistUrl = playlistUrl;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }
}
