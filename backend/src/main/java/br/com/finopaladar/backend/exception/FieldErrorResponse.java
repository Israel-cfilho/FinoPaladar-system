package br.com.finopaladar.backend.exception;

public record FieldErrorResponse(
        String field,
        String message
) {
}
