package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.Cidade;
import jakarta.validation.constraints.Size;

public record PedidoEnderecoRequest(
        Cidade cidade,

        @Size(max = 40)
        String tipoEndereco,

        @Size(max = 120)
        String condominio,

        @Size(max = 50)
        String quadra,

        @Size(max = 50)
        String lote,

        @Size(max = 120)
        String bairro,

        @Size(max = 150)
        String rua,

        @Size(max = 30)
        String numero,

        @Size(max = 150)
        String complemento,

        @Size(max = 150)
        String pontoReferencia
) {
}
