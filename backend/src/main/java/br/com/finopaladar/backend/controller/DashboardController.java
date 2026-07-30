package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.config.OpenApiConfig;
import br.com.finopaladar.backend.dto.DashboardResumoResponse;
import br.com.finopaladar.backend.exception.ApiErrorResponse;
import br.com.finopaladar.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Dashboard", description = "Resumo administrativo de pedidos e faturamento.")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(
            summary = "Consultar resumo do dashboard",
            description = "Retorna contadores de pedidos e valor vendido hoje para o painel administrativo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo retornado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DashboardResumoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Administrador nao autenticado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<DashboardResumoResponse> buscarResumo() {
        return ResponseEntity.ok(dashboardService.buscarResumo());
    }
}
