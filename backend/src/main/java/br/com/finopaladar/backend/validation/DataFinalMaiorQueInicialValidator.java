package br.com.finopaladar.backend.validation;

import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DataFinalMaiorQueInicialValidator
        implements ConstraintValidator<DataFinalMaiorQueInicial, DisponibilidadeProdutoRequest> {

    @Override
    public boolean isValid(DisponibilidadeProdutoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.dataInicial() == null || request.dataFinal() == null) {
            return true;
        }

        boolean periodoValido = request.dataFinal().isAfter(request.dataInicial());
        if (!periodoValido) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("dataFinal")
                    .addConstraintViolation();
        }

        return periodoValido;
    }
}
