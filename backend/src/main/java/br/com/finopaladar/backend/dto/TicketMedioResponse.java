package br.com.finopaladar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TicketMedioResponse(
        LocalDate dataInicial,
        LocalDate dataFinal,
        BigDecimal valor
) {
}
