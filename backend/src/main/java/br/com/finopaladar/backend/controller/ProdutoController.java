package br.com.finopaladar.backend.controller;

import br.com.finopaladar.backend.config.OpenApiConfig;
import br.com.finopaladar.backend.dto.ProdutoRequest;
import br.com.finopaladar.backend.dto.ProdutoPublicoResponse;
import br.com.finopaladar.backend.dto.ProdutoResponse;
import br.com.finopaladar.backend.exception.ApiErrorResponse;
import br.com.finopaladar.backend.service.ProdutoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
@Tag(name = "Produtos", description = "Catalogo publico e administracao de produtos.")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/produtos")
    @Operation(
            summary = "Listar produtos publicos",
            description = "Retorna somente produtos ativos com disponibilidade vigente para exibicao aos clientes."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Produtos disponiveis retornados.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ProdutoPublicoResponse.class))
            )
    )
    public ResponseEntity<List<ProdutoPublicoResponse>> listarProdutosPublicos() {
        return ResponseEntity.ok(produtoService.listarProdutosPublicos());
    }

    @GetMapping("/produtos/{id}")
    @Operation(
            summary = "Buscar produto publico",
            description = "Retorna um produto ativo e disponivel para o cliente pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProdutoPublicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado ou sem disponibilidade.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ProdutoPublicoResponse> buscarProdutoPublico(
            @Parameter(description = "ID do produto.", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(produtoService.buscarProdutoPublicoPorId(id));
    }

    @PostMapping("/admin/produtos")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Criar produto",
            description = "Cadastra um produto na area administrativa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProdutoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ProdutoResponse> criarProduto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do produto.",
                    required = true
            )
            @Valid @RequestBody ProdutoRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        ProdutoResponse response = produtoService.criar(request);
        URI location = uriComponentsBuilder
                .path("/api/admin/produtos/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/admin/produtos")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Listar produtos administrativos",
            description = "Retorna todos os produtos cadastrados, incluindo ativos e inativos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos retornados.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProdutoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ProdutoResponse>> listarProdutosAdmin() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    @GetMapping("/admin/produtos/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Buscar produto administrativo",
            description = "Retorna os dados completos de um produto pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProdutoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ProdutoResponse> buscarProdutoAdmin(
            @Parameter(description = "ID do produto.", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/admin/produtos/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza nome, descricao, preco, peso medio, imagem e status ativo de um produto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProdutoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @Parameter(description = "ID do produto.", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do produto.",
                    required = true
            )
            @Valid @RequestBody ProdutoRequest request
    ) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @PostMapping(value = "/admin/produtos/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Enviar imagem do produto",
            description = "Recebe uma imagem via multipart/form-data, salva o arquivo e grava a URL no produto. Nao usa Base64."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem atualizada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProdutoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente, grande demais ou em formato invalido.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ProdutoResponse> atualizarImagemProduto(
            @Parameter(description = "ID do produto.", required = true)
            @PathVariable Long id,
            @Parameter(description = "Arquivo da imagem. Aceita image/jpeg, image/png e image/webp.", required = true)
            @RequestParam("imagem") MultipartFile imagem
    ) {
        return ResponseEntity.ok(produtoService.atualizarImagem(id, imagem));
    }

    @DeleteMapping("/admin/produtos/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(
            summary = "Inativar produto",
            description = "Inativa um produto para que ele nao apareca no catalogo publico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto inativado.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Administrador nao autenticado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> excluirProduto(
            @Parameter(description = "ID do produto.", required = true)
            @PathVariable Long id
    ) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
