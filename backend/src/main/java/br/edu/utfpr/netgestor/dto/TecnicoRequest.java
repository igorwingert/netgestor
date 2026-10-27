package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotBlank(message = "A especialidade é obrigatória")
        @Size(max = 60, message = "A especialidade deve ter no máximo 60 caracteres")
        String especialidade,

        /* usuário de login do técnico (perfil TECNICO), relacionamento 1:1 */
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId) {
}
