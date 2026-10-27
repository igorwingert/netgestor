package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para cadastrar/atualizar um usuário.
 * A senha é obrigatória também no PUT, porque o PUT substitui o recurso inteiro.
 */
public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        // o BCrypt considera no máximo 72 bytes da senha
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha,

        @NotNull(message = "O perfil é obrigatório (ADMIN, ATENDENTE ou TECNICO)")
        Perfil perfil,

        /* opcional: se não for enviado, o usuário fica ativo */
        Boolean ativo) {

    /** Construtor compacto do record: normaliza o e-mail antes da validação. */
    public UsuarioRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
