package br.edu.utfpr.netgestor.dto.validacao;

import br.edu.utfpr.netgestor.domain.enums.TipoPessoa;
import br.edu.utfpr.netgestor.dto.ClienteRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** implementa a regra da anotacao @DocumentoValido (calculo dos digitos verificadores). */
public class DocumentoValidoValidator implements ConstraintValidator<DocumentoValido, ClienteRequest> {

    @Override
    public boolean isValid(ClienteRequest cliente, ConstraintValidatorContext contexto) {
        // campo vazio ja e tratado por @NotBlank/@NotNull; aqui so validamos o conteudo
        if (cliente.cpfCnpj() == null || cliente.cpfCnpj().isBlank() || cliente.tipoPessoa() == null) {
            return true;
        }

        boolean valido = cliente.tipoPessoa() == TipoPessoa.FISICA
                ? cpfValido(cliente.cpfCnpj())
                : cnpjValido(cliente.cpfCnpj());

        if (!valido) {
            // associa o erro ao campo cpfCnpj (assim o frontend sabe onde exibir a mensagem)
            String mensagem = cliente.tipoPessoa() == TipoPessoa.FISICA
                    ? "CPF inválido para pessoa física"
                    : "CNPJ inválido para pessoa jurídica";
            contexto.disableDefaultConstraintViolation();
            contexto.buildConstraintViolationWithTemplate(mensagem)
                    .addPropertyNode("cpfCnpj")
                    .addConstraintViolation();
        }
        return valido;
    }

    static boolean cpfValido(String cpf) {
        // 11 digitos e nao pode ser sequencia repetida (111.111.111-11 passa no calculo, mas e invalido)
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0'
                && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    /**
     * CNPJ numerico ou alfanumerico (formato da Receita desde jul/2026, migration V3):
     * 12 caracteres de 0-9/A-Z seguidos de 2 digitos verificadores numericos.
     */
    static boolean cnpjValido(String cnpj) {
        if (!cnpj.matches("[0-9A-Z]{12}\\d{2}") || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        return digitoCnpj(cnpj, 12) == cnpj.charAt(12) - '0'
                && digitoCnpj(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    /** CPF: multiplica os digitos por pesos decrescentes (10, 9, 8... ou 11, 10, 9...). */
    private static int digitoCpf(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /**
     * CNPJ: pesos de 2 a 9, repetindo, aplicados da direita para a esquerda.
     * cada caractere vale (codigo ASCII - 48): os digitos continuam valendo 0 a 9
     * e as letras passam a valer A = 17, B = 18 ... Z = 42 (regra da Receita Federal).
     * como '0' tem codigo 48, "caractere - '0'" e exatamente essa conta.
     */
    private static int digitoCnpj(String cnpj, int quantidade) {
        int soma = 0;
        int peso = 2;
        for (int i = quantidade - 1; i >= 0; i--) {
            soma += (cnpj.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
