package br.edu.utfpr.netgestor.dto.validacao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Teste unitário (sem Spring e sem banco) do cálculo dos dígitos verificadores. */
class DocumentoValidoValidatorTest {

    @Test
    void cpf() {
        assertThat(DocumentoValidoValidator.cpfValido("12345678909")).isTrue();
        assertThat(DocumentoValidoValidator.cpfValido("12345678900")).isFalse();  // dígito errado
        assertThat(DocumentoValidoValidator.cpfValido("11111111111")).isFalse();  // sequência repetida
        assertThat(DocumentoValidoValidator.cpfValido("1234567890A")).isFalse();  // CPF não tem letras
    }

    @Test
    void cnpjNumericoContinuaValido() {
        assertThat(DocumentoValidoValidator.cnpjValido("11222333000181")).isTrue();
        assertThat(DocumentoValidoValidator.cnpjValido("11222333000182")).isFalse();
    }

    /** Exemplo oficial da Receita Federal: 12.ABC.345/01DE-35 (migration V3). */
    @Test
    void cnpjAlfanumerico() {
        assertThat(DocumentoValidoValidator.cnpjValido("12ABC34501DE35")).isTrue();
        assertThat(DocumentoValidoValidator.cnpjValido("12ABC34501DE36")).isFalse(); // dígito errado
        assertThat(DocumentoValidoValidator.cnpjValido("12ABC34501DEA5")).isFalse(); // verificador tem de ser número
        assertThat(DocumentoValidoValidator.cnpjValido("12abc34501de35")).isFalse(); // minúsculas: o DTO converte antes
    }
}
