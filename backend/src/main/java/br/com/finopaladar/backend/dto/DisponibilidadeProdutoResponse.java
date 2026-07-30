package br.com.finopaladar.backend.dto;

import java.time.LocalDate;

public record DisponibilidadeProdutoResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        Integer quantidadeDisponivel,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
