package br.edu.utfpr.netgestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados de entrada do endereço. O cliente vem da URL: POST /api/clientes/{clienteId}/enderecos. */
public record EnderecoRequest(
        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 dígitos")
        String cep,

        @NotBlank(message = "O logradouro é obrigatório")
        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres")
        String logradouro,

        @NotBlank(message = "O número é obrigatório")
        @Size(max = 10, message = "O número deve ter no máximo 10 caracteres")
        String numero,

        @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres")
        String complemento,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
        String cidade,

        @NotBlank(message = "A UF é obrigatória")
        @Pattern(regexp = "[A-Z]{2}", message = "A UF deve ter 2 letras (ex.: PR)")
        String uf) {

    /** Aceita CEP com ou sem hífen e UF em minúsculas. */
    public EnderecoRequest {
        if (cep != null) {
            cep = cep.replaceAll("\\D", "");
        }
        if (uf != null) {
            uf = uf.trim().toUpperCase();
        }
    }
}
