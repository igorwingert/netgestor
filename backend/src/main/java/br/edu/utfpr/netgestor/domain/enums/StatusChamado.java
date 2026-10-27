package br.edu.utfpr.netgestor.domain.enums;

/** Ciclo de vida do chamado: ABERTO -> EM_ATENDIMENTO -> RESOLVIDO -> FECHADO. */
public enum StatusChamado {
    ABERTO,
    EM_ATENDIMENTO,
    RESOLVIDO,
    FECHADO;

    public boolean podeIniciarAtendimento() {
        return this == ABERTO;
    }

    public boolean podeResolver() {
        return this == EM_ATENDIMENTO;
    }

    public boolean podeFechar() {
        return this == RESOLVIDO;
    }
}
