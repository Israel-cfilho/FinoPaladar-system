package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        BigDecimal precoUnitario,
        Integer pesoMedioGramas,
        Integer quantidade,
        BigDecimal subtotal
) {
}
