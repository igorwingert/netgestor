package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OltRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
        String nome,

        @NotBlank(message = "O IP de gerência é obrigatório")
        @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
                message = "IP de gerência inválido (use o formato IPv4, ex.: 10.10.0.1)")
        String ipGerencia,

        @NotBlank(message = "O fabricante é obrigatório")
        @Size(max = 60, message = "O fabricante deve ter no máximo 60 caracteres")
        String fabricante,

        @Size(max = 150, message = "A localização deve ter no máximo 150 caracteres")
        String localizacao,

        @NotNull(message = "O total de portas PON é obrigatório")
        @Positive(message = "O total de portas PON deve ser maior que zero")
        Integer totalPortasPon) {
}
