package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record PedidoStatusRequest(
        @NotNull
        StatusPedido status
) {
}
