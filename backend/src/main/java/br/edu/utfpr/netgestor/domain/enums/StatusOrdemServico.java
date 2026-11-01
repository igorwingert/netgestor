package br.edu.utfpr.netgestor.domain.enums;

/** situacao da OS: AGENDADA -> EM_EXECUCAO -> CONCLUIDA; pode ser CANCELADA antes de concluir. */
public enum StatusOrdemServico {
    AGENDADA,
    EM_EXECUCAO,
    CONCLUIDA,
    CANCELADA;

    public boolean podeIniciar() {
        return this == AGENDADA;
    }

    public boolean podeConcluir() {
        return this == EM_EXECUCAO;
    }

    public boolean podeCancelar() {
        return this == AGENDADA || this == EM_EXECUCAO;
    }

    /** OS concluida ou cancelada e historico: nao pode mais ser editada. */
    public boolean podeEditar() {
        return this == AGENDADA || this == EM_EXECUCAO;
    }
}
