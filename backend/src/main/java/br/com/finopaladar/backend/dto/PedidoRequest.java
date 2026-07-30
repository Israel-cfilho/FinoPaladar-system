package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.FormaPagamento;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PedidoRequest(
        @NotBlank
        @Size(max = 150)
        String cliente,

        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[0-9+()\\s-]{8,30}$")
        String telefone,

        @NotNull
        FormaPagamento formaPagamento,

        @NotNull
        TipoRecebimento tipoRecebimento,

        String observacao,

        @Valid
        PedidoEnderecoRequest enderecoEntrega,

        @NotEmpty
        List<@Valid PedidoItemRequest> itens
) {
}
