package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioFaturamentoResponse(
        LocalDate dataInicial,
        LocalDate dataFinal,
        BigDecimal valorTotal
) {
}
