package br.com.finopaladar.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DataFinalMaiorQueInicialValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataFinalMaiorQueInicial {

    String message() default "dataFinal deve ser maior que dataInicial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
