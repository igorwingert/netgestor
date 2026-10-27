package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Dados de entrada do contrato. O número é gerado pelo sistema e o status começa ATIVO
 * (as mudanças de status são feitas por ações próprias, não pelo PUT).
 */
public record ContratoRequest(
        @NotNull(message = "O cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O plano é obrigatório")
        Long planoId,

        @NotNull(message = "O endereço de instalação é obrigatório")
        Long enderecoInstalacaoId,

        /* obrigatórios para planos de FIBRA; não devem ser enviados para planos via RADIO */
        Long ctoId,

        @Positive(message = "A porta da CTO deve ser maior que zero")
        Integer portaCto,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio,

        @NotNull(message = "O dia de vencimento é obrigatório")
        @Min(value = 1, message = "O dia de vencimento deve estar entre 1 e 28")
        @Max(value = 28, message = "O dia de vencimento deve estar entre 1 e 28")
        Integer diaVencimento) {
}
