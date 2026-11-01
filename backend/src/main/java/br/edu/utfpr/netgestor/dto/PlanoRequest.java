package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.Tecnologia;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        String nome,

        @NotNull(message = "A velocidade de download é obrigatória")
        @Positive(message = "A velocidade de download deve ser maior que zero")
        Integer downloadMbps,

        @NotNull(message = "A velocidade de upload é obrigatória")
        @Positive(message = "A velocidade de upload deve ser maior que zero")
        Integer uploadMbps,

        @NotNull(message = "O valor mensal é obrigatório")
        @Positive(message = "O valor mensal deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O valor mensal deve ter no máximo 2 casas decimais")
        BigDecimal valorMensal,

        @NotNull(message = "A tecnologia é obrigatória (FIBRA ou RADIO)")
        Tecnologia tecnologia,

        /* opcional: se nao for enviado, o plano fica ativo */
        Boolean ativo) {
}
