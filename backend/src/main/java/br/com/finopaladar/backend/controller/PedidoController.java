package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.config.OpenApiConfig;
import br.com.finopaladar.backend.dto.HistoricoStatusResponse;
import br.com.finopaladar.backend.dto.PedidoRequest;
import br.com.finopaladar.backend.dto.PedidoResponse;
import br.com.finopaladar.backend.dto.PedidoStatusRequest;
import br.com.finopaladar.backend.dto.VendaManualRequest;
import br.com.finopaladar.backend.exception.ApiErrorResponse;
import br.com.finopaladar.backend.service.PedidoService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
@Tag(name = "Pedidos", description = "Criacao, acompanhamento e administracao de pedidos.")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/pedidos")
    @Operation(
            summary = "Criar pedido publico",
            description = "Cria um pedido do cliente, valida produtos e disponibilidade, calcula totais, baixa estoque e retorna mensagem/link do WhatsApp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou regra de negocio violada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> criarPedido(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do pedido enviados pelo cliente.",
                    required = true
            )
            @Valid @RequestBody PedidoRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        PedidoResponse response = pedidoService.criar(request);
        URI location = uriComponentsBuilder
                .path("/api/pedidos/{codigo}")
                .buildAndExpand(response.codigo())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/pedidos/{codigo}")
    @Operation(
            summary = "Buscar pedido por codigo",
            description = "Permite ao cliente consultar os dados e status de um pedido pelo codigo gerado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> buscarPedido(
            @Parameter(description = "Codigo publico do pedido.", required = true)
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(pedidoService.buscarPorCodigo(codigo));
    }

    @GetMapping("/pedidos/{codigo}/historico")
    @Operation(
            summary = "Listar historico do pedido",
            description = "Retorna a linha do tempo de status de um pedido pelo codigo publico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historico retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = HistoricoStatusResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<HistoricoStatusResponse>> listarHistorico(
            @Parameter(description = "Codigo publico do pedido.", required = true)
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(pedidoService.listarHistoricoPorCodigo(codigo));
    }

    @PostMapping("/admin/pedidos/vendas-manuais")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Registrar venda manual",
            description = "Registra pedido feito por WhatsApp, telefone ou presencialmente. A venda nasce como ENTREGUE e entra no faturamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venda manual registrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou regra de negocio violada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponse> registrarVendaManual(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da venda manual e canal de origem.",
                    required = true
            )
            @Valid @RequestBody VendaManualRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        PedidoResponse response = pedidoService.registrarVendaManual(request);
        URI location = uriComponentsBuilder
                .path("/api/pedidos/{codigo}")
                .buildAndExpand(response.codigo())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/admin/pedidos/{id}/status")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Alterar status do pedido",
            description = "Altera o status de um pedido respeitando os fluxos permitidos e registra historico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status alterado.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Status obrigatorio ou transicao invalida.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> alterarStatus(
            @Parameter(description = "ID interno do pedido.", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novo status do pedido.",
                    required = true
            )
            @Valid @RequestBody PedidoStatusRequest request
    ) {
        pedidoService.alterarStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}
