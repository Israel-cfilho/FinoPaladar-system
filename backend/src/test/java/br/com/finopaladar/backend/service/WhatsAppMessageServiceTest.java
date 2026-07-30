package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.finopaladar.backend.entity.Cidade;
import br.com.finopaladar.backend.entity.EnderecoEntrega;
import br.com.finopaladar.backend.entity.FormaPagamento;
import br.com.finopaladar.backend.entity.ItemPedido;
import br.com.finopaladar.backend.entity.Pedido;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WhatsAppMessageServiceTest {

    @Test
    void deveGerarMensagemELinkComNumeroConfigurado() {
        WhatsAppMessageService service = new WhatsAppMessageService("+55 (83) 99999-9999");
        Pedido pedido = pedidoEntrega();

        WhatsAppMessageService.WhatsAppPedido response = service.gerarParaPedido(pedido);

        assertThat(response.mensagem())
                .contains(
                        "Pedido Fino Paladar",
                        "Codigo: PED-TESTE",
                        "Cliente: Maria Silva",
                        "Cidade: BANANEIRAS",
                        "- 2x Bolo de Rolo (500g) - R$ 45,90 cada = R$ 91,80",
                        "Total: R$ 99,80"
                );
        assertThat(response.linkWhatsApp()).startsWith("https://wa.me/5583999999999?text=");
        assertThat(textoDoLink(response.linkWhatsApp())).isEqualTo(response.mensagem());
    }

    @Test
    void deveGerarLinkGenericoQuandoNumeroNaoConfigurado() {
        WhatsAppMessageService service = new WhatsAppMessageService("");

        WhatsAppMessageService.WhatsAppPedido response = service.gerarParaPedido(pedidoRetirada());

        assertThat(response.linkWhatsApp()).startsWith("https://wa.me/?text=");
        assertThat(textoDoLink(response.linkWhatsApp())).isEqualTo(response.mensagem());
    }

    private String textoDoLink(String linkWhatsApp) {
        return URLDecoder.decode(linkWhatsApp.substring(linkWhatsApp.indexOf("text=") + 5), StandardCharsets.UTF_8);
    }

    private Pedido pedidoRetirada() {
        Pedido pedido = pedidoBase(TipoRecebimento.RETIRADA);
        pedido.setTaxaEntrega(BigDecimal.ZERO);
        pedido.setValorTotal(new BigDecimal("91.80"));
        return pedido;
    }

    private Pedido pedidoEntrega() {
        Pedido pedido = pedidoBase(TipoRecebimento.ENTREGA);
        pedido.setTaxaEntrega(new BigDecimal("8.00"));
        pedido.setValorTotal(new BigDecimal("99.80"));

        EnderecoEntrega endereco = new EnderecoEntrega();
        endereco.setCidade(Cidade.BANANEIRAS);
        endereco.setTipoEndereco("RUA");
        endereco.setBairro("Centro");
        endereco.setRua("Rua Principal");
        endereco.setNumero("123");
        endereco.setComplemento("Casa");
        endereco.setPontoReferencia("Perto da praca");
        pedido.setEnderecoEntrega(endereco);

        return pedido;
    }

    private Pedido pedidoBase(TipoRecebimento tipoRecebimento) {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Bolo de Rolo");
        produto.setPreco(new BigDecimal("45.90"));
        produto.setPesoMedioGramas(500);
        produto.setAtivo(true);

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCodigo("PED-TESTE");
        pedido.setCliente("Maria Silva");
        pedido.setTelefone("83999999999");
        pedido.setStatus(StatusPedido.AGUARDANDO_CONFIRMACAO);
        pedido.setFormaPagamento(FormaPagamento.PIX);
        pedido.setTipoRecebimento(tipoRecebimento);
        pedido.setValorProdutos(new BigDecimal("91.80"));
        pedido.setData(LocalDate.of(2026, 8, 1));
        pedido.setObservacao("Sem observacoes");

        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setId(1L);
        itemPedido.setProduto(produto);
        itemPedido.setNomeProduto(produto.getNome());
        itemPedido.setPrecoUnitario(produto.getPreco());
        itemPedido.setPesoMedioGramas(produto.getPesoMedioGramas());
        itemPedido.setQuantidade(2);
        itemPedido.setSubtotal(new BigDecimal("91.80"));
        pedido.adicionarItem(itemPedido);

        return pedido;
    }
}
