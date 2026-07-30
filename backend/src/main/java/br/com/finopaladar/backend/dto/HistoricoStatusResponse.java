package br.com.finopaladar.backend.dto;

import br.com.finopaladar.backend.entity.StatusPedido;
import java.time.Instant;

public record HistoricoStatusResponse(
        Long id,
        StatusPedido status,
        Instant dataHora
) {
}
