package br.edu.utfpr.netgestor.exception;

/**
 * lancada quando a operacao viola uma regra de negocio
 * (dado duplicado, exclusao com dependentes, porta da CTO ocupada...). vira HTTP 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
