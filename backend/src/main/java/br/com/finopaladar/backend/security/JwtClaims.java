package br.com.finopaladar.backend.security;

import java.time.Instant;

public record JwtClaims(
        String subject,
        Instant expiraEm
) {
}
