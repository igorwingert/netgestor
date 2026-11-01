package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.NotBlank;

/** corpo do POST /api/auth/login. */
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha) {
}
