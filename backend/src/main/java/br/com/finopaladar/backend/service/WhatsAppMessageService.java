package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.entity.EnderecoEntrega;
import br.com.finopaladar.backend.entity.ItemPedido;
import br.com.finopaladar.backend.entity.Pedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageService {

    private static final String QUEBRA_LINHA = "\n";
    private static final String LINK_BASE = "https://wa.me/";

    private final String numeroWhatsApp;

    public WhatsAppMessageService(@Value("${whatsapp.numero:}") String numeroWhatsApp) {
        this.numeroWhatsApp = somenteDigitos(numeroWhatsApp);
    }

    public WhatsAppPedido gerarParaPedido(Pedido pedido) {
        String mensagem = montarMensagem(pedido);
        return new WhatsAppPedido(mensagem, montarLink(mensagem));
    }

    private String montarMensagem(Pedido pedido) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Pedido Fino Paladar").append(QUEBRA_LINHA);
        mensagem.append("Codigo: ").append(pedido.getCodigo()).append(QUEBRA_LINHA);
        mensagem.append("Cliente: ").append(pedido.getCliente()).append(QUEBRA_LINHA);
        mensagem.append("Telefone: ").append(pedido.getTelefone()).append(QUEBRA_LINHA);
        mensagem.append("Forma de pagamento: ").append(pedido.getFormaPagamento()).append(QUEBRA_LINHA);
        mensagem.append("Tipo de recebimento: ").append(pedido.getTipoRecebimento()).append(QUEBRA_LINHA);

        adicionarEndereco(mensagem, pedido);
        adicionarItens(mensagem, pedido);
        adicionarTotais(mensagem, pedido);
        adicionarObservacao(mensagem, pedido);

        return mensagem.toString().trim();
    }

    private void adicionarEndereco(StringBuilder mensagem, Pedido pedido) {
        if (pedido.getTipoRecebimento() != TipoRecebimento.ENTREGA || pedido.getEnderecoEntrega() == null) {
            return;
        }

        EnderecoEntrega endereco = pedido.getEnderecoEntrega();
        mensagem.append(QUEBRA_LINHA).append("Endereco de entrega:").append(QUEBRA_LINHA);
        mensagem.append("Cidade: ").append(endereco.getCidade()).append(QUEBRA_LINHA);
        adicionarLinhaSePreenchida(mensagem, "Tipo: ", endereco.getTipoEndereco());
        adicionarLinhaSePreenchida(mensagem, "Condominio: ", endereco.getCondominio());
        adicionarLinhaSePreenchida(mensagem, "Quadra: ", endereco.getQuadra());
        adicionarLinhaSePreenchida(mensagem, "Lote: ", endereco.getLote());
        adicionarLinhaSePreenchida(mensagem, "Bairro: ", endereco.getBairro());
        adicionarLinhaSePreenchida(mensagem, "Rua: ", endereco.getRua());
        adicionarLinhaSePreenchida(mensagem, "Numero: ", endereco.getNumero());
        adicionarLinhaSePreenchida(mensagem, "Complemento: ", endereco.getComplemento());
        adicionarLinhaSePreenchida(mensagem, "Referencia: ", endereco.getPontoReferencia());
    }

    private void adicionarItens(StringBuilder mensagem, Pedido pedido) {
        mensagem.append(QUEBRA_LINHA).append("Itens:").append(QUEBRA_LINHA);
        pedido.getItens()
                .stream()
                .sorted(Comparator.comparing(ItemPedido::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(item -> mensagem
                        .append("- ")
                        .append(item.getQuantidade())
                        .append("x ")
                        .append(item.getNomeProduto())
                        .append(" (")
                        .append(item.getPesoMedioGramas())
                        .append("g) - ")
                        .append(formatarMoeda(item.getPrecoUnitario()))
                        .append(" cada = ")
                        .append(formatarMoeda(item.getSubtotal()))
                        .append(QUEBRA_LINHA)
                );
    }

    private void adicionarTotais(StringBuilder mensagem, Pedido pedido) {
        mensagem.append(QUEBRA_LINHA);
        mensagem.append("Subtotal: ").append(formatarMoeda(pedido.getValorProdutos())).append(QUEBRA_LINHA);
        mensagem.append("Taxa de entrega: ").append(formatarMoeda(pedido.getTaxaEntrega())).append(QUEBRA_LINHA);
        mensagem.append("Total: ").append(formatarMoeda(pedido.getValorTotal())).append(QUEBRA_LINHA);
    }

    private void adicionarObservacao(StringBuilder mensagem, Pedido pedido) {
        if (!isBlank(pedido.getObservacao())) {
            mensagem.append("Observacoes: ").append(pedido.getObservacao().trim()).append(QUEBRA_LINHA);
        }
    }

    private String montarLink(String mensagem) {
        String texto = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
        if (numeroWhatsApp.isBlank()) {
            return LINK_BASE + "?text=" + texto;
        }

        return LINK_BASE + numeroWhatsApp + "?text=" + texto;
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;
        return "R$ " + valorSeguro.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",");
    }

    private void adicionarLinhaSePreenchida(StringBuilder mensagem, String rotulo, String valor) {
        if (!isBlank(valor)) {
            mensagem.append(rotulo).append(valor.trim()).append(QUEBRA_LINHA);
        }
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private String somenteDigitos(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }

    public record WhatsAppPedido(
            String mensagem,
            String linkWhatsApp
    ) {
    }
}
