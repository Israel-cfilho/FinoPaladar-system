package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer pesoMedioGramas,
        String imagem,
        Boolean ativo
) {
}
