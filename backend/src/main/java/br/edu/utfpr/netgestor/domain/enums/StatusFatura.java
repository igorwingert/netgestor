package br.edu.utfpr.netgestor.domain.enums;

/** situacao da fatura mensal. */
public enum StatusFatura {
    ABERTA,
    PAGA,
    VENCIDA,
    CANCELADA;

    /** so fatura em aberto (no prazo ou vencida) recebe pagamento ou pode ser cancelada. */
    public boolean emAberto() {
        return this == ABERTA || this == VENCIDA;
    }
}
