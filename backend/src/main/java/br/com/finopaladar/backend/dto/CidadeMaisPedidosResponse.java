package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.Cidade;

public record CidadeMaisPedidosResponse(
        Cidade cidade,
        Long quantidadePedidos
) {
}
