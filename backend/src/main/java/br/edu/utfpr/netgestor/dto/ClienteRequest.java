package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.TipoPessoa;
import br.edu.utfpr.netgestor.dto.validacao.DocumentoValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados de entrada do cliente. @DocumentoValido confere o CPF/CNPJ de acordo com o tipo de pessoa. */
@DocumentoValido
public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        String cpfCnpj,

        @NotNull(message = "O tipo de pessoa é obrigatório (FISICA ou JURIDICA)")
        TipoPessoa tipoPessoa,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        /* opcional: se não for enviado, o cliente fica ativo */
        Boolean ativo) {

    /** Aceita CPF/CNPJ com ou sem máscara: guarda somente os dígitos. */
    public ClienteRequest {
        if (cpfCnpj != null) {
            cpfCnpj = cpfCnpj.replaceAll("\\D", "");
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
