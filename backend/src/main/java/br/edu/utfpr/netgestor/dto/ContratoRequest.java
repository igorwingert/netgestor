package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * dados de entrada do contrato. o numero e gerado pelo sistema e o status comeca ATIVO
 * (as mudancas de status sao feitas por acoes proprias, nao pelo PUT).
 */
public record ContratoRequest(
        @NotNull(message = "O cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O plano é obrigatório")
        Long planoId,

        @NotNull(message = "O endereço de instalação é obrigatório")
        Long enderecoInstalacaoId,

        /* obrigatorios para planos de FIBRA; nao devem ser enviados para planos via RADIO */
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
