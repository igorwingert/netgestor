package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * dados de entrada da fatura. a competencia e o mes de referencia no formato "2026-11".
 * valor e vencimento sao opcionais: se nao forem enviados, usam o valor do plano
 * e o dia de vencimento do contrato.
 */
public record FaturaRequest(
        @NotNull(message = "O contrato é obrigatório")
        Long contratoId,

        @NotNull(message = "A competência é obrigatória (formato AAAA-MM)")
        YearMonth competencia,

        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        LocalDate dataVencimento) {
}
