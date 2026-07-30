package br.com.finopaladar.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoItemRequest(
        @NotNull
        Long produtoId,

        @NotNull
        @Positive
        Integer quantidade
) {
}
