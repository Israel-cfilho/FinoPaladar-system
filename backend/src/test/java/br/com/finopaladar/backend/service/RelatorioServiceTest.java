package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse;
import br.com.finopaladar.backend.dto.ProdutoVendidoResponse;
import br.com.finopaladar.backend.dto.RelatorioFaturamentoResponse;
import br.com.finopaladar.backend.dto.TicketMedioResponse;
import br.com.finopaladar.backend.entity.Cidade;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.repository.RelatorioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        relatorioService = new RelatorioService(relatorioRepository);
    }

    @Test
    void deveCalcularFaturamentoDiario() {
        LocalDate data = LocalDate.of(2026, 7, 29);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, data, data))
                .thenReturn(new BigDecimal("150.00"));

        RelatorioFaturamentoResponse response = relatorioService.faturamentoDiario(data);

        assertThat(response.dataInicial()).isEqualTo(data);
        assertThat(response.dataFinal()).isEqualTo(data);
        assertThat(response.valorTotal()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveCalcularFaturamentoSemanalDeSegundaADomingo() {
        LocalDate dataReferencia = LocalDate.of(2026, 7, 29);
        LocalDate dataInicial = LocalDate.of(2026, 7, 27);
        LocalDate dataFinal = LocalDate.of(2026, 8, 2);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(new BigDecimal("700.00"));

        RelatorioFaturamentoResponse response = relatorioService.faturamentoSemanal(dataReferencia);

        assertThat(response.dataInicial()).isEqualTo(dataInicial);
        assertThat(response.dataFinal()).isEqualTo(dataFinal);
        assertThat(response.valorTotal()).isEqualByComparingTo("700.00");
    }

    @Test
    void deveCalcularFaturamentoMensal() {
        LocalDate dataInicial = LocalDate.of(2026, 2, 1);
        LocalDate dataFinal = LocalDate.of(2026, 2, 28);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(new BigDecimal("1200.00"));

        RelatorioFaturamentoResponse response = relatorioService.faturamentoMensal(2026, 2);

        assertThat(response.dataInicial()).isEqualTo(dataInicial);
        assertThat(response.dataFinal()).isEqualTo(dataFinal);
        assertThat(response.valorTotal()).isEqualByComparingTo("1200.00");
    }

    @Test
    void deveCalcularFaturamentoAnual() {
        LocalDate dataInicial = LocalDate.of(2026, 1, 1);
        LocalDate dataFinal = LocalDate.of(2026, 12, 31);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(new BigDecimal("25000.00"));

        RelatorioFaturamentoResponse response = relatorioService.faturamentoAnual(2026);

        assertThat(response.dataInicial()).isEqualTo(dataInicial);
        assertThat(response.dataFinal()).isEqualTo(dataFinal);
        assertThat(response.valorTotal()).isEqualByComparingTo("25000.00");
    }

    @Test
    void deveCalcularFaturamentoPorPeriodoPersonalizado() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(new BigDecimal("3200.00"));

        RelatorioFaturamentoResponse response = relatorioService.faturamentoPorPeriodo(dataInicial, dataFinal);

        assertThat(response.dataInicial()).isEqualTo(dataInicial);
        assertThat(response.dataFinal()).isEqualTo(dataFinal);
        assertThat(response.valorTotal()).isEqualByComparingTo("3200.00");
    }

    @Test
    void deveRejeitarPeriodoInvalido() {
        assertThatThrownBy(() -> relatorioService.faturamentoPorPeriodo(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 7, 31)
                ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data final deve ser maior ou igual a data inicial");
    }

    @Test
    void deveListarProdutosMaisVendidosComLimite() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        ProdutoVendidoResponse produto = new ProdutoVendidoResponse(
                1L,
                "Bolo de Rolo",
                8L,
                new BigDecimal("367.20")
        );
        when(relatorioRepository.findProdutosMaisVendidosByStatusAndPeriodo(
                eq(StatusPedido.ENTREGUE),
                eq(dataInicial),
                eq(dataFinal),
                any(Pageable.class)
        )).thenReturn(List.of(produto));

        List<ProdutoVendidoResponse> response = relatorioService.produtosMaisVendidos(dataInicial, dataFinal, 5);

        assertThat(response).containsExactly(produto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(relatorioRepository).findProdutosMaisVendidosByStatusAndPeriodo(
                eq(StatusPedido.ENTREGUE),
                eq(dataInicial),
                eq(dataFinal),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void deveListarQuantidadeVendidaPorProduto() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        ProdutoVendidoResponse produto = new ProdutoVendidoResponse(
                1L,
                "Bolo de Rolo",
                8L,
                new BigDecimal("367.20")
        );
        when(relatorioRepository.findQuantidadeVendidaPorProdutoByStatusAndPeriodo(
                StatusPedido.ENTREGUE,
                dataInicial,
                dataFinal
        )).thenReturn(List.of(produto));

        List<ProdutoVendidoResponse> response = relatorioService.quantidadeVendidaPorProduto(dataInicial, dataFinal);

        assertThat(response).containsExactly(produto);
    }

    @Test
    void deveCalcularTicketMedio() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(new BigDecimal("301.00"));
        when(relatorioRepository.countPedidosByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(3L);

        TicketMedioResponse response = relatorioService.ticketMedio(dataInicial, dataFinal);

        assertThat(response.dataInicial()).isEqualTo(dataInicial);
        assertThat(response.dataFinal()).isEqualTo(dataFinal);
        assertThat(response.valor()).isEqualByComparingTo("100.33");
    }

    @Test
    void deveRetornarTicketMedioZeroQuandoNaoHouverPedidosEntregues() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioRepository.sumValorTotalByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(null);
        when(relatorioRepository.countPedidosByStatusAndPeriodo(StatusPedido.ENTREGUE, dataInicial, dataFinal))
                .thenReturn(0L);

        TicketMedioResponse response = relatorioService.ticketMedio(dataInicial, dataFinal);

        assertThat(response.valor()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deveRetornarCidadeComMaisPedidos() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        CidadeMaisPedidosResponse cidade = new CidadeMaisPedidosResponse(Cidade.BANANEIRAS, 12L);
        when(relatorioRepository.findCidadesComMaisPedidosByStatusAndPeriodo(
                eq(StatusPedido.ENTREGUE),
                eq(dataInicial),
                eq(dataFinal),
                any(Pageable.class)
        )).thenReturn(List.of(cidade));

        CidadeMaisPedidosResponse response = relatorioService.cidadeComMaisPedidos(dataInicial, dataFinal);

        assertThat(response).isEqualTo(cidade);
    }

    @Test
    void deveRetornarCidadeNulaQuandoNaoHouverPedidosComEntrega() {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioRepository.findCidadesComMaisPedidosByStatusAndPeriodo(
                eq(StatusPedido.ENTREGUE),
                eq(dataInicial),
                eq(dataFinal),
                any(Pageable.class)
        )).thenReturn(List.of());

        CidadeMaisPedidosResponse response = relatorioService.cidadeComMaisPedidos(dataInicial, dataFinal);

        assertThat(response.cidade()).isNull();
        assertThat(response.quantidadePedidos()).isZero();
    }
}
