package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.CanalVenda;
import br.com.finopaladar.backend.entity.FormaPagamento;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigo,
        String cliente,
        String telefone,
        StatusPedido status,
        FormaPagamento formaPagamento,
        TipoRecebimento tipoRecebimento,
        CanalVenda canalVenda,
        BigDecimal valorProdutos,
        BigDecimal taxaEntrega,
        BigDecimal valorTotal,
        String observacao,
        LocalDate data,
        PedidoEnderecoResponse enderecoEntrega,
        List<PedidoItemResponse> itens,
        String mensagem,
        String linkWhatsApp
) {
}
