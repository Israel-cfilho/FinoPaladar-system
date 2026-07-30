package br.com.finopaladar.backend.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEm,
        AdministradorAutenticadoResponse administrador
) {
}
