package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CtoRequest(
        @NotBlank(message = "A identificação é obrigatória")
        @Size(max = 30, message = "A identificação deve ter no máximo 30 caracteres")
        String identificacao,

        @NotNull(message = "A OLT é obrigatória")
        Long oltId,

        @NotNull(message = "A porta PON é obrigatória")
        @Positive(message = "A porta PON deve ser maior que zero")
        Integer portaPon,

        @NotNull(message = "A capacidade é obrigatória")
        @Positive(message = "A capacidade deve ser maior que zero")
        Integer capacidade,

        @NotNull(message = "A latitude é obrigatória")
        @DecimalMin(value = "-90", message = "A latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90", message = "A latitude deve estar entre -90 e 90")
        BigDecimal latitude,

        @NotNull(message = "A longitude é obrigatória")
        @DecimalMin(value = "-180", message = "A longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180", message = "A longitude deve estar entre -180 e 180")
        BigDecimal longitude) {
}
