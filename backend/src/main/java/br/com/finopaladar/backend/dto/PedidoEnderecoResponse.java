package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.Cidade;

public record PedidoEnderecoResponse(
        Cidade cidade,
        String tipoEndereco,
        String condominio,
        String quadra,
        String lote,
        String bairro,
        String rua,
        String numero,
        String complemento,
        String pontoReferencia
) {
}
