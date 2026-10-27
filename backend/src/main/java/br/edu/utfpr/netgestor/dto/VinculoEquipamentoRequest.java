package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.NotNull;

/** Corpo da ação PATCH /api/equipamentos/{id}/vincular: o contrato que recebe o equipamento em comodato. */
public record VinculoEquipamentoRequest(
        @NotNull(message = "O contrato é obrigatório")
        Long contratoId) {
}
