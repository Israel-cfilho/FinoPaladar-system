package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.DashboardResumoResponse;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(pedidoRepository);
    }

    @Test
    void deveRetornarResumoDashboard() {
        when(pedidoRepository.countByData(any(LocalDate.class))).thenReturn(6L);
        when(pedidoRepository.countByStatusIn(any())).thenReturn(4L);
        when(pedidoRepository.countByStatus(StatusPedido.ENTREGUE)).thenReturn(10L);
        when(pedidoRepository.countByStatus(StatusPedido.CANCELADO)).thenReturn(2L);
        when(pedidoRepository.sumValorTotalByDataAndStatus(any(LocalDate.class), eq(StatusPedido.ENTREGUE)))
                .thenReturn(new BigDecimal("250.50"));

        DashboardResumoResponse response = dashboardService.buscarResumo();

        assertThat(response.pedidosHoje()).isEqualTo(6);
        assertThat(response.pedidosEmAberto()).isEqualTo(4);
        assertThat(response.pedidosEntregues()).isEqualTo(10);
        assertThat(response.pedidosCancelados()).isEqualTo(2);
        assertThat(response.valorVendidoHoje()).isEqualByComparingTo("250.50");

        ArgumentCaptor<Collection<StatusPedido>> statusCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(pedidoRepository).countByStatusIn(statusCaptor.capture());
        assertThat(statusCaptor.getValue())
                .containsExactly(
                        StatusPedido.AGUARDANDO_CONFIRMACAO,
                        StatusPedido.ACEITO,
                        StatusPedido.EM_PREPARACAO,
                        StatusPedido.PRONTO_PARA_RETIRADA,
                        StatusPedido.SAIU_PARA_ENTREGA
                );
    }

    @Test
    void deveRetornarValorVendidoHojeZeroQuandoNaoHouverVendas() {
        when(pedidoRepository.sumValorTotalByDataAndStatus(any(LocalDate.class), eq(StatusPedido.ENTREGUE)))
                .thenReturn(null);

        DashboardResumoResponse response = dashboardService.buscarResumo();

        assertThat(response.valorVendidoHoje()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
