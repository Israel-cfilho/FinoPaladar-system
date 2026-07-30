package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;

public record DashboardResumoResponse(
        long pedidosHoje,
        long pedidosEmAberto,
        long pedidosEntregues,
        long pedidosCancelados,
        BigDecimal valorVendidoHoje
) {
}
