package br.edu.utfpr.netgestor.exception;

/** lancada quando o id informado nao existe no banco. vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
