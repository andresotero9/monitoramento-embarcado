package br.com.monitoramento.exception;

/**
 * Lançada quando um recurso solicitado não existe. Mapeada para HTTP 404
 * pelo {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
