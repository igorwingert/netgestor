package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * dados de entrada para cadastrar/atualizar um usuario.
 * a senha e obrigatoria tambem no PUT, porque o PUT substitui o recurso inteiro.
 */
public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        // o BCrypt considera no maximo 72 bytes da senha
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha,

        @NotNull(message = "O perfil é obrigatório (ADMIN, ATENDENTE ou TECNICO)")
        Perfil perfil,

        /* opcional: se nao for enviado, o usuario fica ativo */
        Boolean ativo) {

    /** construtor compacto do record: normaliza o e-mail antes da validacao. */
    public UsuarioRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
