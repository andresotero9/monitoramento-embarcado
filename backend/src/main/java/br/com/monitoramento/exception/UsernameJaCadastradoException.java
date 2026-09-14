package br.com.monitoramento.exception;

/**
 * Lançada ao tentar cadastrar um usuário com um username já existente.
 * Mapeada para HTTP 409 pelo {@link GlobalExceptionHandler}.
 */
public class UsernameJaCadastradoException extends RuntimeException {

    public UsernameJaCadastradoException(String message) {
        super(message);
    }
}
