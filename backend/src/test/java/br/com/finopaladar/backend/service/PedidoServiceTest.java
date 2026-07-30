package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.HistoricoStatusResponse;
import br.com.finopaladar.backend.dto.PedidoEnderecoRequest;
import br.com.finopaladar.backend.dto.PedidoItemRequest;
import br.com.finopaladar.backend.dto.PedidoRequest;
import br.com.finopaladar.backend.dto.PedidoResponse;
import br.com.finopaladar.backend.dto.PedidoStatusRequest;
import br.com.finopaladar.backend.dto.VendaManualRequest;
import br.com.finopaladar.backend.entity.CanalVenda;
import br.com.finopaladar.backend.entity.Cidade;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.FormaPagamento;
import br.com.finopaladar.backend.entity.HistoricoStatus;
import br.com.finopaladar.backend.entity.ItemPedido;
import br.com.finopaladar.backend.entity.Pedido;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.PedidoMapper;
import br.com.finopaladar.backend.repository.DisponibilidadeProdutoRepository;
import br.com.finopaladar.backend.repository.HistoricoStatusRepository;
import br.com.finopaladar.backend.repository.PedidoRepository;
import br.com.finopaladar.backend.repository.ProdutoRepository;
import br.com.finopaladar.backend.util.PedidoCodigoGenerator;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private DisponibilidadeProdutoRepository disponibilidadeProdutoRepository;

    @Mock
    private HistoricoStatusRepository historicoStatusRepository;

    @Mock
    private PedidoCodigoGenerator pedidoCodigoGenerator;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                produtoRepository,
                disponibilidadeProdutoRepository,
                historicoStatusRepository,
                new PedidoMapper(),
                pedidoCodigoGenerator,
                new WhatsAppMessageService("5583999999999"),
                new BigDecimal("8.00")
        );
    }

    @Test
    void deveCriarPedidoDeRetiradaCalculandoTotaisEbaixandoDisponibilidade() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 5);
        PedidoRequest request = pedidoRequest(TipoRecebimento.RETIRADA, null, List.of(new PedidoItemRequest(1L, 2)));

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> salvarPedido(invocation.getArgument(0)));

        PedidoResponse response = pedidoService.criar(request);

        assertThat(response.codigo()).isEqualTo("PED-TESTE");
        assertThat(response.status()).isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
        assertThat(response.canalVenda()).isNull();
        assertThat(response.valorProdutos()).isEqualByComparingTo("91.80");
        assertThat(response.taxaEntrega()).isEqualByComparingTo("0.00");
        assertThat(response.valorTotal()).isEqualByComparingTo("91.80");
        assertThat(response.enderecoEntrega()).isNull();
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().getFirst().nomeProduto()).isEqualTo("Bolo de Rolo");
        assertThat(response.itens().getFirst().subtotal()).isEqualByComparingTo("91.80");
        assertThat(response.mensagem())
                .contains(
                        "Codigo: PED-TESTE",
                        "Cliente: Maria Silva",
                        "Tipo de recebimento: RETIRADA",
                        "- 2x Bolo de Rolo (500g) - R$ 45,90 cada = R$ 91,80",
                        "Total: R$ 91,80",
                        "Observacoes: Sem observacoes"
                );
        assertThat(response.linkWhatsApp()).startsWith("https://wa.me/5583999999999?text=");
        assertThat(textoDoLink(response.linkWhatsApp())).isEqualTo(response.mensagem());
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(3);

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertThat(pedidoCaptor.getValue().getHistoricoStatus()).hasSize(1);
        assertThat(pedidoCaptor.getValue().getHistoricoStatus().getFirst().getStatus())
                .isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
    }

    @Test
    void deveRegistrarVendaManualComoEntregueCalculandoTotaisEBaixandoDisponibilidade() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 5);
        VendaManualRequest request = vendaManualRequest(
                CanalVenda.WHATSAPP,
                TipoRecebimento.RETIRADA,
                null,
                List.of(new PedidoItemRequest(1L, 2))
        );

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> salvarPedido(invocation.getArgument(0)));

        PedidoResponse response = pedidoService.registrarVendaManual(request);

        assertThat(response.codigo()).isEqualTo("PED-TESTE");
        assertThat(response.status()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(response.canalVenda()).isEqualTo(CanalVenda.WHATSAPP);
        assertThat(response.valorProdutos()).isEqualByComparingTo("91.80");
        assertThat(response.taxaEntrega()).isEqualByComparingTo("0.00");
        assertThat(response.valorTotal()).isEqualByComparingTo("91.80");
        assertThat(response.mensagem()).isNull();
        assertThat(response.linkWhatsApp()).isNull();
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(3);

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertThat(pedidoCaptor.getValue().getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedidoCaptor.getValue().getCanalVenda()).isEqualTo(CanalVenda.WHATSAPP);
        assertThat(pedidoCaptor.getValue().getHistoricoStatus()).hasSize(1);
        assertThat(pedidoCaptor.getValue().getHistoricoStatus().getFirst().getStatus())
                .isEqualTo(StatusPedido.ENTREGUE);
    }

    @Test
    void deveRegistrarVendaManualDeEntregaComTaxaEEndereco() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 5);
        VendaManualRequest request = vendaManualRequest(
                CanalVenda.TELEFONE,
                TipoRecebimento.ENTREGA,
                enderecoComum(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> salvarPedido(invocation.getArgument(0)));

        PedidoResponse response = pedidoService.registrarVendaManual(request);

        assertThat(response.status()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(response.canalVenda()).isEqualTo(CanalVenda.TELEFONE);
        assertThat(response.valorProdutos()).isEqualByComparingTo("45.90");
        assertThat(response.taxaEntrega()).isEqualByComparingTo("8.00");
        assertThat(response.valorTotal()).isEqualByComparingTo("53.90");
        assertThat(response.enderecoEntrega()).isNotNull();
        assertThat(response.enderecoEntrega().cidade()).isEqualTo(Cidade.BANANEIRAS);
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(4);
    }

    @Test
    void deveRejeitarVendaManualSemCanal() {
        VendaManualRequest request = vendaManualRequest(
                null,
                TipoRecebimento.RETIRADA,
                null,
                List.of(new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.registrarVendaManual(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Canal da venda manual deve ser informado");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveCriarPedidoDeEntregaComTaxaEEndereco() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 5);
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                enderecoComum(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> salvarPedido(invocation.getArgument(0)));

        PedidoResponse response = pedidoService.criar(request);

        assertThat(response.valorProdutos()).isEqualByComparingTo("45.90");
        assertThat(response.taxaEntrega()).isEqualByComparingTo("8.00");
        assertThat(response.valorTotal()).isEqualByComparingTo("53.90");
        assertThat(response.enderecoEntrega()).isNotNull();
        assertThat(response.enderecoEntrega().cidade()).isEqualTo(Cidade.BANANEIRAS);
        assertThat(response.mensagem())
                .contains(
                        "Endereco de entrega:",
                        "Cidade: BANANEIRAS",
                        "Bairro: Centro",
                        "Rua: Rua Principal",
                        "Numero: 123"
                );
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(4);
    }

    @Test
    void deveCriarPedidoDeEntregaEmCondominio() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 5);
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                enderecoCondominio(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> salvarPedido(invocation.getArgument(0)));

        PedidoResponse response = pedidoService.criar(request);

        assertThat(response.enderecoEntrega()).isNotNull();
        assertThat(response.enderecoEntrega().cidade()).isEqualTo(Cidade.SOLANEA);
        assertThat(response.enderecoEntrega().tipoEndereco()).isEqualTo("CONDOMINIO");
        assertThat(response.enderecoEntrega().condominio()).isEqualTo("Residencial Vale");
        assertThat(response.enderecoEntrega().quadra()).isEqualTo("A");
        assertThat(response.enderecoEntrega().lote()).isEqualTo("12");
        assertThat(response.valorTotal()).isEqualByComparingTo("53.90");
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(4);
    }

    @Test
    void deveRejeitarEntregaSemEndereco() {
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                null,
                List.of(new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Endereco de entrega deve ser informado");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarEntregaSemCidade() {
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                enderecoSemCidade(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cidade deve ser informada para entrega");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarCondominioSemCamposObrigatorios() {
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                enderecoCondominioSemCamposObrigatorios(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Condominio, quadra e lote devem ser informados");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarEnderecoComumSemCamposObrigatorios() {
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.ENTREGA,
                enderecoComumSemCamposObrigatorios(),
                List.of(new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bairro, rua e numero devem ser informados");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarProdutoInexistente() {
        PedidoRequest request = pedidoRequest(TipoRecebimento.RETIRADA, null, List.of(new PedidoItemRequest(99L, 1)));

        configurarCodigoUnico();
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarProdutoInativo() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", false);
        PedidoRequest request = pedidoRequest(TipoRecebimento.RETIRADA, null, List.of(new PedidoItemRequest(1L, 1)));

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Produto inativo nao pode ser comprado");

        verify(disponibilidadeProdutoRepository, never()).findDisponibilidadeVigenteComLock(any(), any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarProdutoSemDisponibilidadeVigente() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        PedidoRequest request = pedidoRequest(TipoRecebimento.RETIRADA, null, List.of(new PedidoItemRequest(1L, 1)));

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Produto indisponivel");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarQuantidadeMaiorQueDisponibilidade() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(produto, 1);
        PedidoRequest request = pedidoRequest(TipoRecebimento.RETIRADA, null, List.of(new PedidoItemRequest(1L, 2)));

        configurarCodigoUnico();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(disponibilidadeProduto));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Quantidade indisponivel para o produto Bolo de Rolo");

        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(1);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarProdutoDuplicadoNoPedido() {
        PedidoRequest request = pedidoRequest(
                TipoRecebimento.RETIRADA,
                null,
                List.of(new PedidoItemRequest(1L, 1), new PedidoItemRequest(1L, 1))
        );

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Produto duplicado no pedido");

        verify(pedidoCodigoGenerator, never()).gerar();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveBuscarPedidoPorCodigo() {
        Pedido pedido = pedidoSalvoBasico();
        when(pedidoRepository.findByCodigo("PED-TESTE")).thenReturn(Optional.of(pedido));

        PedidoResponse response = pedidoService.buscarPorCodigo("PED-TESTE");

        assertThat(response.codigo()).isEqualTo("PED-TESTE");
        assertThat(response.status()).isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
        assertThat(response.itens()).hasSize(1);
    }

    @Test
    void deveAlterarStatusRegistrandoHistorico() {
        Pedido pedido = pedidoSalvoBasico();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ACEITO));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ACEITO);
        assertThat(pedido.getHistoricoStatus()).hasSize(1);
        assertThat(pedido.getHistoricoStatus().getFirst().getStatus()).isEqualTo(StatusPedido.ACEITO);
        assertThat(pedido.getHistoricoStatus().getFirst().getDataHora()).isNotNull();

        verify(disponibilidadeProdutoRepository, never()).findByProdutoIdComLock(any());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void devePermitirFluxoAteProntoParaRetiradaEEntregue() {
        Pedido pedido = pedidoSalvoBasico();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ACEITO));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.EM_PREPARACAO));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.PRONTO_PARA_RETIRADA));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ENTREGUE));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getHistoricoStatus())
                .extracting(HistoricoStatus::getStatus)
                .containsExactly(
                        StatusPedido.ACEITO,
                        StatusPedido.EM_PREPARACAO,
                        StatusPedido.PRONTO_PARA_RETIRADA,
                        StatusPedido.ENTREGUE
                );
    }

    @Test
    void devePermitirFluxoAteSaiuParaEntregaEEntregue() {
        Pedido pedido = pedidoSalvoBasico();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ACEITO));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.EM_PREPARACAO));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.SAIU_PARA_ENTREGA));
        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ENTREGUE));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getHistoricoStatus())
                .extracting(HistoricoStatus::getStatus)
                .containsExactly(
                        StatusPedido.ACEITO,
                        StatusPedido.EM_PREPARACAO,
                        StatusPedido.SAIU_PARA_ENTREGA,
                        StatusPedido.ENTREGUE
                );
    }

    @Test
    void deveCancelarPedidoAguardandoConfirmacaoDevolvendoDisponibilidadeERegistrandoHistorico() {
        Pedido pedido = pedidoSalvoBasico();
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(pedido.getItens().getFirst().getProduto(), 3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(disponibilidadeProdutoRepository.findByProdutoIdComLock(1L))
                .thenReturn(Optional.of(disponibilidadeProduto));

        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.CANCELADO));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(4);
        assertThat(pedido.getHistoricoStatus())
                .extracting(HistoricoStatus::getStatus)
                .containsExactly(StatusPedido.CANCELADO);

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void deveCancelarPedidoDevolvendoDisponibilidadeERegistrandoHistorico() {
        Pedido pedido = pedidoSalvoBasico();
        pedido.setStatus(StatusPedido.ACEITO);
        DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProduto(pedido.getItens().getFirst().getProduto(), 3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(disponibilidadeProdutoRepository.findByProdutoIdComLock(1L))
                .thenReturn(Optional.of(disponibilidadeProduto));

        pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.CANCELADO));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(disponibilidadeProduto.getQuantidadeDisponivel()).isEqualTo(4);
        assertThat(pedido.getHistoricoStatus()).hasSize(1);
        assertThat(pedido.getHistoricoStatus().getFirst().getStatus()).isEqualTo(StatusPedido.CANCELADO);

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void deveRejeitarCancelamentoQuandoPedidoJaEstaEmPreparacao() {
        Pedido pedido = pedidoSalvoBasico();
        pedido.setStatus(StatusPedido.EM_PREPARACAO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.CANCELADO)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transicao de status invalida");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PREPARACAO);
        assertThat(pedido.getHistoricoStatus()).isEmpty();
        verify(disponibilidadeProdutoRepository, never()).findByProdutoIdComLock(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVoltarStatus() {
        Pedido pedido = pedidoSalvoBasico();
        pedido.setStatus(StatusPedido.EM_PREPARACAO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ACEITO)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transicao de status invalida");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PREPARACAO);
        assertThat(pedido.getHistoricoStatus()).isEmpty();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarTrocaEntreFluxoRetiradaEEntrega() {
        Pedido pedido = pedidoSalvoBasico();
        pedido.setStatus(StatusPedido.PRONTO_PARA_RETIRADA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.SAIU_PARA_ENTREGA)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transicao de status invalida");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PRONTO_PARA_RETIRADA);
        assertThat(pedido.getHistoricoStatus()).isEmpty();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarTransicaoStatusInvalida() {
        Pedido pedido = pedidoSalvoBasico();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.alterarStatus(1L, new PedidoStatusRequest(StatusPedido.ENTREGUE)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Transicao de status invalida");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
        assertThat(pedido.getHistoricoStatus()).isEmpty();
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveListarHistoricoPorCodigoOrdenadoPorDataHora() {
        Instant primeiraDataHora = Instant.parse("2026-08-01T10:15:00Z");
        Instant segundaDataHora = Instant.parse("2026-08-01T10:30:00Z");
        when(pedidoRepository.existsByCodigo("PED-TESTE")).thenReturn(true);
        when(historicoStatusRepository.findByPedidoCodigoOrderByDataHoraAsc("PED-TESTE"))
                .thenReturn(List.of(
                        historicoStatus(1L, StatusPedido.AGUARDANDO_CONFIRMACAO, primeiraDataHora),
                        historicoStatus(2L, StatusPedido.ACEITO, segundaDataHora)
                ));

        List<HistoricoStatusResponse> response = pedidoService.listarHistoricoPorCodigo("PED-TESTE");

        assertThat(response).hasSize(2);
        assertThat(response.getFirst().status()).isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
        assertThat(response.getFirst().dataHora()).isEqualTo(primeiraDataHora);
        assertThat(response.get(1).status()).isEqualTo(StatusPedido.ACEITO);
        assertThat(response.get(1).dataHora()).isEqualTo(segundaDataHora);
    }

    @Test
    void deveRejeitarHistoricoDePedidoInexistente() {
        when(pedidoRepository.existsByCodigo("PED-INEXISTENTE")).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.listarHistoricoPorCodigo("PED-INEXISTENTE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pedido nao encontrado");

        verify(historicoStatusRepository, never()).findByPedidoCodigoOrderByDataHoraAsc(any());
    }

    private void configurarCodigoUnico() {
        when(pedidoCodigoGenerator.gerar()).thenReturn("PED-TESTE");
        when(pedidoRepository.existsByCodigo("PED-TESTE")).thenReturn(false);
    }

    private String textoDoLink(String linkWhatsApp) {
        return URLDecoder.decode(linkWhatsApp.substring(linkWhatsApp.indexOf("text=") + 5), StandardCharsets.UTF_8);
    }

    private PedidoRequest pedidoRequest(
            TipoRecebimento tipoRecebimento,
            PedidoEnderecoRequest enderecoEntrega,
            List<PedidoItemRequest> itens
    ) {
        return new PedidoRequest(
                "Maria Silva",
                "83999999999",
                FormaPagamento.PIX,
                tipoRecebimento,
                "Sem observacoes",
                enderecoEntrega,
                itens
        );
    }

    private VendaManualRequest vendaManualRequest(
            CanalVenda canalVenda,
            TipoRecebimento tipoRecebimento,
            PedidoEnderecoRequest enderecoEntrega,
            List<PedidoItemRequest> itens
    ) {
        return new VendaManualRequest(
                canalVenda,
                "Maria Silva",
                "83999999999",
                FormaPagamento.PIX,
                tipoRecebimento,
                "Sem observacoes",
                enderecoEntrega,
                itens
        );
    }

    private PedidoEnderecoRequest enderecoComum() {
        return new PedidoEnderecoRequest(
                Cidade.BANANEIRAS,
                "RUA",
                null,
                null,
                null,
                "Centro",
                "Rua Principal",
                "123",
                "Casa",
                "Perto da praca"
        );
    }

    private PedidoEnderecoRequest enderecoCondominio() {
        return new PedidoEnderecoRequest(
                Cidade.SOLANEA,
                "CONDOMINIO",
                "Residencial Vale",
                "A",
                "12",
                null,
                null,
                null,
                "Casa 2",
                "Portaria principal"
        );
    }

    private PedidoEnderecoRequest enderecoSemCidade() {
        return new PedidoEnderecoRequest(
                null,
                "RUA",
                null,
                null,
                null,
                "Centro",
                "Rua Principal",
                "123",
                null,
                null
        );
    }

    private PedidoEnderecoRequest enderecoCondominioSemCamposObrigatorios() {
        return new PedidoEnderecoRequest(
                Cidade.BANANEIRAS,
                "CONDOMINIO",
                null,
                "A",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PedidoEnderecoRequest enderecoComumSemCamposObrigatorios() {
        return new PedidoEnderecoRequest(
                Cidade.BANANEIRAS,
                "RUA",
                null,
                null,
                null,
                "",
                "Rua Principal",
                null,
                null,
                null
        );
    }

    private Produto produto(Long id, String nome, String preco, Boolean ativo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setDescricao("Tradicional");
        produto.setPreco(new BigDecimal(preco));
        produto.setPesoMedioGramas(500);
        produto.setImagem("https://example.com/bolo.jpg");
        produto.setAtivo(ativo);
        return produto;
    }

    private DisponibilidadeProduto disponibilidadeProduto(Produto produto, Integer quantidadeDisponivel) {
        DisponibilidadeProduto disponibilidadeProduto = new DisponibilidadeProduto();
        disponibilidadeProduto.setId(1L);
        disponibilidadeProduto.setProduto(produto);
        disponibilidadeProduto.setQuantidadeDisponivel(quantidadeDisponivel);
        disponibilidadeProduto.setDataInicial(LocalDate.now().minusDays(1));
        disponibilidadeProduto.setDataFinal(LocalDate.now().plusDays(1));
        return disponibilidadeProduto;
    }

    private Pedido salvarPedido(Pedido pedido) {
        pedido.setId(1L);
        long itemId = 1L;
        for (ItemPedido itemPedido : pedido.getItens()) {
            itemPedido.setId(itemId++);
        }
        return pedido;
    }

    private Pedido pedidoSalvoBasico() {
        Produto produto = produto(1L, "Bolo de Rolo", "45.90", true);

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCodigo("PED-TESTE");
        pedido.setCliente("Maria Silva");
        pedido.setTelefone("83999999999");
        pedido.setStatus(StatusPedido.AGUARDANDO_CONFIRMACAO);
        pedido.setFormaPagamento(FormaPagamento.PIX);
        pedido.setTipoRecebimento(TipoRecebimento.RETIRADA);
        pedido.setValorProdutos(new BigDecimal("45.90"));
        pedido.setTaxaEntrega(BigDecimal.ZERO);
        pedido.setValorTotal(new BigDecimal("45.90"));
        pedido.setData(LocalDate.now());

        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setId(1L);
        itemPedido.setProduto(produto);
        itemPedido.setNomeProduto(produto.getNome());
        itemPedido.setPrecoUnitario(produto.getPreco());
        itemPedido.setPesoMedioGramas(produto.getPesoMedioGramas());
        itemPedido.setQuantidade(1);
        itemPedido.setSubtotal(produto.getPreco());
        pedido.adicionarItem(itemPedido);

        return pedido;
    }

    private HistoricoStatus historicoStatus(Long id, StatusPedido status, Instant dataHora) {
        HistoricoStatus historicoStatus = new HistoricoStatus();
        historicoStatus.setId(id);
        historicoStatus.setStatus(status);
        historicoStatus.setDataHora(dataHora);
        return historicoStatus;
    }
}
