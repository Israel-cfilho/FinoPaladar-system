package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.validation.DataFinalMaiorQueInicial;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@DataFinalMaiorQueInicial
public record DisponibilidadeProdutoRequest(
        @NotNull
        Long produtoId,

        @NotNull
        @PositiveOrZero
        Integer quantidadeDisponivel,

        @NotNull
        LocalDate dataInicial,

        @NotNull
        LocalDate dataFinal
) {
}
