package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.FormaPagamento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** dados de entrada do pagamento. a fatura vem da URL: POST /api/faturas/{faturaId}/pagamentos. */
public record PagamentoRequest(
        @NotNull(message = "A data do pagamento é obrigatória")
        @PastOrPresent(message = "A data do pagamento não pode estar no futuro")
        LocalDate dataPagamento,

        @NotNull(message = "O valor pago é obrigatório")
        @Positive(message = "O valor pago deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O valor pago deve ter no máximo 2 casas decimais")
        BigDecimal valorPago,

        @NotNull(message = "A forma de pagamento é obrigatória (PIX, BOLETO ou CARTAO)")
        FormaPagamento forma) {
}
