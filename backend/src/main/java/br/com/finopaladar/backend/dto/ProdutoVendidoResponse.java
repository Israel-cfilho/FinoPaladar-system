package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;

public record ProdutoVendidoResponse(
        Long produtoId,
        String nomeProduto,
        Long quantidadeVendida,
        BigDecimal valorVendido
) {
}
