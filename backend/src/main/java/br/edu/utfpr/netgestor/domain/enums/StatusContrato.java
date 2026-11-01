package br.edu.utfpr.netgestor.domain.enums;

/**
 * situacao do contrato. transicoes: ATIVO <-> SUSPENSO; ATIVO/SUSPENSO -> CANCELADO.
 *
 * os metodos "pode..." sao a fonte unica das regras de transicao: o service usa
 * para recusar a acao (409) e o assembler usa para decidir quais links exibir.
 */
public enum StatusContrato {
    ATIVO,
    SUSPENSO,
    CANCELADO;

    public boolean podeSuspender() {
        return this == ATIVO;
    }

    public boolean podeReativar() {
        return this == SUSPENSO;
    }

    public boolean podeCancelar() {
        return this != CANCELADO;
    }
}
