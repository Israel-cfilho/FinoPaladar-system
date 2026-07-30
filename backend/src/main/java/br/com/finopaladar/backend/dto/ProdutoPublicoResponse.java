package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;

public record ProdutoPublicoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Integer pesoMedioGramas,
        String imagem,
        Integer quantidadeDisponivel
) {
}
