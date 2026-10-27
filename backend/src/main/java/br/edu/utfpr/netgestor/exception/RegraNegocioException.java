package br.edu.utfpr.netgestor.exception;

/**
 * Lançada quando a operação viola uma regra de negócio
 * (dado duplicado, exclusão com dependentes, porta da CTO ocupada...). Vira HTTP 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
