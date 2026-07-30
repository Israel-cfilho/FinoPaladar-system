package br.com.finopaladar.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank
        @Size(max = 150)
        String nome,

        String descricao,

        @NotNull
        @Positive
        BigDecimal preco,

        @NotNull
        @Positive
        Integer pesoMedioGramas,

        @NotBlank
        @Size(max = 500)
        String imagem,

        @NotNull
        Boolean ativo
) {
}
