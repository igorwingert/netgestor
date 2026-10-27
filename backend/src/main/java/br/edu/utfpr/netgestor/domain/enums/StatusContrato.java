package br.edu.utfpr.netgestor.domain.enums;

/** Situação do contrato. Transições: ATIVO <-> SUSPENSO; ATIVO/SUSPENSO -> CANCELADO. */
public enum StatusContrato {
    ATIVO,
    SUSPENSO,
    CANCELADO
}
