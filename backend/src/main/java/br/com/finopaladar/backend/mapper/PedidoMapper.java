package br.com.finopaladar.backend.mapper;

import br.com.finopaladar.backend.dto.HistoricoStatusResponse;
import br.com.finopaladar.backend.dto.PedidoEnderecoRequest;
import br.com.finopaladar.backend.dto.PedidoEnderecoResponse;
import br.com.finopaladar.backend.dto.PedidoItemResponse;
import br.com.finopaladar.backend.dto.PedidoRequest;
import br.com.finopaladar.backend.dto.PedidoResponse;
import br.com.finopaladar.backend.entity.EnderecoEntrega;
import br.com.finopaladar.backend.entity.HistoricoStatus;
import br.com.finopaladar.backend.entity.ItemPedido;
import br.com.finopaladar.backend.entity.Pedido;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    public Pedido toEntity(PedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setCliente(request.cliente());
        pedido.setTelefone(request.telefone());
        pedido.setFormaPagamento(request.formaPagamento());
        pedido.setTipoRecebimento(request.tipoRecebimento());
        pedido.setObservacao(request.observacao());
        return pedido;
    }

    public EnderecoEntrega toEnderecoEntrega(PedidoEnderecoRequest request) {
        EnderecoEntrega enderecoEntrega = new EnderecoEntrega();
        enderecoEntrega.setCidade(request.cidade());
        enderecoEntrega.setTipoEndereco(request.tipoEndereco());
        enderecoEntrega.setCondominio(request.condominio());
        enderecoEntrega.setQuadra(request.quadra());
        enderecoEntrega.setLote(request.lote());
        enderecoEntrega.setBairro(request.bairro());
        enderecoEntrega.setRua(request.rua());
        enderecoEntrega.setNumero(request.numero());
        enderecoEntrega.setComplemento(request.complemento());
        enderecoEntrega.setPontoReferencia(request.pontoReferencia());
        return enderecoEntrega;
    }

    public PedidoResponse toResponse(Pedido pedido) {
        return toResponse(pedido, null, null);
    }

    public PedidoResponse toResponse(Pedido pedido, String mensagem, String linkWhatsApp) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getCliente(),
                pedido.getTelefone(),
                pedido.getStatus(),
                pedido.getFormaPagamento(),
                pedido.getTipoRecebimento(),
                pedido.getCanalVenda(),
                pedido.getValorProdutos(),
                pedido.getTaxaEntrega(),
                pedido.getValorTotal(),
                pedido.getObservacao(),
                pedido.getData(),
                toEnderecoResponse(pedido.getEnderecoEntrega()),
                pedido.getItens()
                        .stream()
                        .sorted(Comparator.comparing(ItemPedido::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(this::toItemResponse)
                        .toList(),
                mensagem,
                linkWhatsApp
        );
    }

    public HistoricoStatusResponse toHistoricoStatusResponse(HistoricoStatus historicoStatus) {
        return new HistoricoStatusResponse(
                historicoStatus.getId(),
                historicoStatus.getStatus(),
                historicoStatus.getDataHora()
        );
    }

    private PedidoItemResponse toItemResponse(ItemPedido itemPedido) {
        return new PedidoItemResponse(
                itemPedido.getId(),
                itemPedido.getProduto().getId(),
                itemPedido.getNomeProduto(),
                itemPedido.getPrecoUnitario(),
                itemPedido.getPesoMedioGramas(),
                itemPedido.getQuantidade(),
                itemPedido.getSubtotal()
        );
    }

    private PedidoEnderecoResponse toEnderecoResponse(EnderecoEntrega enderecoEntrega) {
        if (enderecoEntrega == null) {
            return null;
        }

        return new PedidoEnderecoResponse(
                enderecoEntrega.getCidade(),
                enderecoEntrega.getTipoEndereco(),
                enderecoEntrega.getCondominio(),
                enderecoEntrega.getQuadra(),
                enderecoEntrega.getLote(),
                enderecoEntrega.getBairro(),
                enderecoEntrega.getRua(),
                enderecoEntrega.getNumero(),
                enderecoEntrega.getComplemento(),
                enderecoEntrega.getPontoReferencia()
        );
    }
}
