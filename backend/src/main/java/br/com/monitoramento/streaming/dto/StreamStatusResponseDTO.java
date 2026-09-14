package br.com.monitoramento.streaming.dto;

import br.com.monitoramento.streaming.StreamStatus;

public class StreamStatusResponseDTO {

    private final Long cameraId;
    private final StreamStatus status;

    public StreamStatusResponseDTO(Long cameraId, StreamStatus status) {
        this.cameraId = cameraId;
        this.status = status;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public StreamStatus getStatus() {
        return status;
    }
}
