package br.com.monitoramento.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: estado inconsistente,
 * conflito de dados). Mapeada para HTTP 409 pelo {@link GlobalExceptionHandler}.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
