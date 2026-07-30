package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.config.OpenApiConfig;
import br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse;
import br.com.finopaladar.backend.dto.ProdutoVendidoResponse;
import br.com.finopaladar.backend.dto.RelatorioFaturamentoResponse;
import br.com.finopaladar.backend.dto.TicketMedioResponse;
import br.com.finopaladar.backend.exception.ApiErrorResponse;
import br.com.finopaladar.backend.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/relatorios")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Relatorios", description = "Consultas administrativas de faturamento e vendas.")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/faturamento/diario")
    @Operation(
            summary = "Consultar faturamento diario",
            description = "Retorna o faturamento confirmado de pedidos ENTREGUE em uma data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faturamento diario retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RelatorioFaturamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parametro data invalido ou ausente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<RelatorioFaturamentoResponse> faturamentoDiario(
            @Parameter(description = "Data no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return ResponseEntity.ok(relatorioService.faturamentoDiario(data));
    }

    @GetMapping("/faturamento/semanal")
    @Operation(
            summary = "Consultar faturamento semanal",
            description = "Retorna o faturamento confirmado da semana da data informada, de segunda a domingo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faturamento semanal retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RelatorioFaturamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parametro data invalido ou ausente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<RelatorioFaturamentoResponse> faturamentoSemanal(
            @Parameter(description = "Data de referencia no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return ResponseEntity.ok(relatorioService.faturamentoSemanal(data));
    }

    @GetMapping("/faturamento/mensal")
    @Operation(
            summary = "Consultar faturamento mensal",
            description = "Retorna o faturamento confirmado de um mes especifico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faturamento mensal retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RelatorioFaturamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ano ou mes invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<RelatorioFaturamentoResponse> faturamentoMensal(
            @Parameter(description = "Ano do faturamento.", required = true)
            @RequestParam @Min(1) Integer ano,
            @Parameter(description = "Mes do faturamento, de 1 a 12.", required = true)
            @RequestParam @Min(1) @Max(12) Integer mes
    ) {
        return ResponseEntity.ok(relatorioService.faturamentoMensal(ano, mes));
    }

    @GetMapping("/faturamento/anual")
    @Operation(
            summary = "Consultar faturamento anual",
            description = "Retorna o faturamento confirmado de um ano."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faturamento anual retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RelatorioFaturamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ano invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<RelatorioFaturamentoResponse> faturamentoAnual(
            @Parameter(description = "Ano do faturamento.", required = true)
            @RequestParam @Min(1) Integer ano
    ) {
        return ResponseEntity.ok(relatorioService.faturamentoAnual(ano));
    }

    @GetMapping("/faturamento")
    @Operation(
            summary = "Consultar faturamento por periodo",
            description = "Retorna o faturamento confirmado em um intervalo personalizado de datas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faturamento do periodo retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RelatorioFaturamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<RelatorioFaturamentoResponse> faturamentoPersonalizado(
            @Parameter(description = "Data inicial no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return ResponseEntity.ok(relatorioService.faturamentoPorPeriodo(dataInicial, dataFinal));
    }

    @GetMapping("/produtos-mais-vendidos")
    @Operation(
            summary = "Listar produtos mais vendidos",
            description = "Retorna os produtos mais vendidos por quantidade em um periodo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos mais vendidos retornados.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProdutoVendidoResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Periodo ou limite invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ProdutoVendidoResponse>> produtosMaisVendidos(
            @Parameter(description = "Data inicial no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @Parameter(description = "Quantidade maxima de produtos retornados. Padrao 10, maximo 100.")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limite
    ) {
        return ResponseEntity.ok(relatorioService.produtosMaisVendidos(dataInicial, dataFinal, limite));
    }

    @GetMapping("/produtos/quantidade-vendida")
    @Operation(
            summary = "Listar quantidade vendida por produto",
            description = "Retorna a quantidade vendida e faturamento por produto em um periodo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantidade vendida retornada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProdutoVendidoResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ProdutoVendidoResponse>> quantidadeVendidaPorProduto(
            @Parameter(description = "Data inicial no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return ResponseEntity.ok(relatorioService.quantidadeVendidaPorProduto(dataInicial, dataFinal));
    }

    @GetMapping("/ticket-medio")
    @Operation(
            summary = "Consultar ticket medio",
            description = "Calcula o valor medio dos pedidos entregues em um periodo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket medio retornado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TicketMedioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TicketMedioResponse> ticketMedio(
            @Parameter(description = "Data inicial no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return ResponseEntity.ok(relatorioService.ticketMedio(dataInicial, dataFinal));
    }

    @GetMapping("/cidade-mais-pedidos")
    @Operation(
            summary = "Consultar cidade com mais pedidos",
            description = "Retorna a cidade com maior quantidade de pedidos entregues em um periodo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cidade retornada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CidadeMaisPedidosResponse.class))),
            @ApiResponse(responseCode = "400", description = "Periodo invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CidadeMaisPedidosResponse> cidadeComMaisPedidos(
            @Parameter(description = "Data inicial no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final no formato ISO yyyy-MM-dd.", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return ResponseEntity.ok(relatorioService.cidadeComMaisPedidos(dataInicial, dataFinal));
    }
}
