package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.DashboardResumoResponse;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final List<StatusPedido> STATUS_EM_ABERTO = List.of(
            StatusPedido.AGUARDANDO_CONFIRMACAO,
            StatusPedido.ACEITO,
            StatusPedido.EM_PREPARACAO,
            StatusPedido.PRONTO_PARA_RETIRADA,
            StatusPedido.SAIU_PARA_ENTREGA
    );

    private final PedidoRepository pedidoRepository;

    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse buscarResumo() {
        LocalDate hoje = LocalDate.now();
        BigDecimal valorVendidoHoje = pedidoRepository.sumValorTotalByDataAndStatus(hoje, StatusPedido.ENTREGUE);

        return new DashboardResumoResponse(
                pedidoRepository.countByData(hoje),
                pedidoRepository.countByStatusIn(STATUS_EM_ABERTO),
                pedidoRepository.countByStatus(StatusPedido.ENTREGUE),
                pedidoRepository.countByStatus(StatusPedido.CANCELADO),
                valorVendidoHoje == null ? BigDecimal.ZERO : valorVendidoHoje
        );
    }
}
