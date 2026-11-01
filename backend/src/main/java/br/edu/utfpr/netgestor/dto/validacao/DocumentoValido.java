package br.edu.utfpr.netgestor.dto.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * validacao personalizada (Bean Validation) aplicada na classe ClienteRequest:
 * confere o CPF quando o cliente e pessoa FISICA e o CNPJ quando e JURIDICA.
 * fica na classe (e nao no campo) porque depende de dois campos ao mesmo tempo.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DocumentoValidoValidator.class)
public @interface DocumentoValido {

    String message() default "CPF/CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
