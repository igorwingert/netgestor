package br.edu.utfpr.netgestor.domain.enums;

/**
 * Situação do contrato. Transições: ATIVO <-> SUSPENSO; ATIVO/SUSPENSO -> CANCELADO.
 *
 * Os métodos "pode..." são a fonte única das regras de transição: o service usa
 * para recusar a ação (409) e o assembler usa para decidir quais links exibir.
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
