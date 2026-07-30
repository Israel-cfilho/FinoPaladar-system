package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.config.OpenApiConfig;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoResponse;
import br.com.finopaladar.backend.exception.ApiErrorResponse;
import br.com.finopaladar.backend.service.DisponibilidadeProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/disponibilidade")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Disponibilidade", description = "Controle administrativo da disponibilidade dos produtos.")
public class DisponibilidadeProdutoController {

    private final DisponibilidadeProdutoService disponibilidadeProdutoService;

    public DisponibilidadeProdutoController(DisponibilidadeProdutoService disponibilidadeProdutoService) {
        this.disponibilidadeProdutoService = disponibilidadeProdutoService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar disponibilidade",
            description = "Cria uma disponibilidade para produto existente, respeitando quantidade nao negativa e periodo valido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Disponibilidade cadastrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DisponibilidadeProdutoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados invalidos.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DisponibilidadeProdutoResponse> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da disponibilidade do produto.",
                    required = true
            )
            @Valid @RequestBody DisponibilidadeProdutoRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        DisponibilidadeProdutoResponse response = disponibilidadeProdutoService.cadastrar(request);
        URI location = uriComponentsBuilder
                .path("/api/admin/disponibilidade/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar disponibilidades",
            description = "Retorna todas as disponibilidades cadastradas para administracao."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidades retornadas.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DisponibilidadeProdutoResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<DisponibilidadeProdutoResponse>> consultar() {
        return ResponseEntity.ok(disponibilidadeProdutoService.consultar());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar disponibilidade por ID",
            description = "Retorna uma disponibilidade especifica pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade encontrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisponibilidadeProdutoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Disponibilidade nao encontrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DisponibilidadeProdutoResponse> consultarPorId(
            @Parameter(description = "ID da disponibilidade.", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(disponibilidadeProdutoService.consultarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Editar disponibilidade",
            description = "Atualiza quantidade e periodo de uma disponibilidade existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade atualizada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisponibilidadeProdutoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Disponibilidade ou produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DisponibilidadeProdutoResponse> editar(
            @Parameter(description = "ID da disponibilidade.", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados da disponibilidade.",
                    required = true
            )
            @Valid @RequestBody DisponibilidadeProdutoRequest request
    ) {
        return ResponseEntity.ok(disponibilidadeProdutoService.editar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir disponibilidade",
            description = "Remove uma disponibilidade cadastrada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disponibilidade excluida.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Disponibilidade nao encontrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da disponibilidade.", required = true)
            @PathVariable Long id
    ) {
        disponibilidadeProdutoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
