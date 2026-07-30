package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Endpoint publico de verificacao de saude da API.")
public class HealthController {

    @GetMapping
    @Operation(
            summary = "Consultar saude da API",
            description = "Retorna o status basico da aplicacao e o instante atual do servidor."
    )
    @ApiResponse(
            responseCode = "200",
            description = "API operacional.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HealthResponse.class)
            )
    )
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "finopaladar-backend", Instant.now()));
    }
}
