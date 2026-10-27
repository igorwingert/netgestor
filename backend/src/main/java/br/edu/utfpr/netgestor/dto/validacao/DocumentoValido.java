package br.edu.utfpr.netgestor.dto.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validação personalizada (Bean Validation) aplicada na classe ClienteRequest:
 * confere o CPF quando o cliente é pessoa FISICA e o CNPJ quando é JURIDICA.
 * Fica na classe (e não no campo) porque depende de dois campos ao mesmo tempo.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DocumentoValidoValidator.class)
public @interface DocumentoValido {

    String message() default "CPF/CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
