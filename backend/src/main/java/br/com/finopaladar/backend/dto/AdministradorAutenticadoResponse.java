package br.com.finopaladar.backend.dto;

public record AdministradorAutenticadoResponse(
        Long id,
        String nome,
        String email
) {
}
