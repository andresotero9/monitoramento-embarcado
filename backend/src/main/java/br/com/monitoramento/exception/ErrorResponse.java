package br.com.monitoramento.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padronizado de resposta de erro retornado pela API.
 */
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<String> detalhes;

    public ErrorResponse(int status, String error, String message, String path, List<String> detalhes) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.detalhes = detalhes;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<String> getDetalhes() {
        return detalhes;
    }
}
