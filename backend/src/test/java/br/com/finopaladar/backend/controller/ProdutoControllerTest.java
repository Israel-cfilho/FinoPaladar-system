package br.com.finopaladar.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.ProdutoRequest;
import br.com.finopaladar.backend.dto.ProdutoPublicoResponse;
import br.com.finopaladar.backend.dto.ProdutoResponse;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(ProdutoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoService produtoService;

    @Test
    void deveListarProdutosDisponiveisNoCatalogoPublico() throws Exception {
        when(produtoService.listarProdutosPublicos()).thenReturn(List.of(produtoPublicoResponse()));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Bolo de Rolo"))
                .andExpect(jsonPath("$[0].preco").value(45.90))
                .andExpect(jsonPath("$[0].pesoMedioGramas").value(500))
                .andExpect(jsonPath("$[0].imagem").value("https://example.com/bolo.jpg"))
                .andExpect(jsonPath("$[0].quantidadeDisponivel").value(7))
                .andExpect(jsonPath("$[0].ativo").doesNotExist())
                .andExpect(jsonPath("$[0].descricao").doesNotExist());

        verify(produtoService).listarProdutosPublicos();
    }

    @Test
    void deveBuscarProdutoDisponivelNoCatalogoPublico() throws Exception {
        when(produtoService.buscarProdutoPublicoPorId(1L)).thenReturn(produtoPublicoResponse());

        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Bolo de Rolo"))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(7))
                .andExpect(jsonPath("$.ativo").doesNotExist())
                .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarProdutoNaAreaAdministrativa() throws Exception {
        when(produtoService.criar(any(ProdutoRequest.class))).thenReturn(produtoResponse(true));

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest(true))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/admin/produtos/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveBloquearCriacaoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest(true))))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(produtoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'nome')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'preco')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'pesoMedioGramas')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'imagem')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'ativo')]").exists());

        verifyNoInteractions(produtoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarProdutoNaAreaAdministrativa() throws Exception {
        when(produtoService.atualizar(any(Long.class), any(ProdutoRequest.class))).thenReturn(produtoResponse(false));

        mockMvc.perform(put("/api/admin/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarImagemProdutoNaAreaAdministrativa() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem",
                "bolo.png",
                "image/png",
                "conteudo".getBytes()
        );
        when(produtoService.atualizarImagem(any(Long.class), any(MultipartFile.class)))
                .thenReturn(produtoResponseComImagem("/uploads/produtos/bolo.png"));

        mockMvc.perform(multipart("/api/admin/produtos/1/imagem").file(imagem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imagem").value("/uploads/produtos/bolo.png"));

        verify(produtoService).atualizarImagem(any(Long.class), any(MultipartFile.class));
    }

    @Test
    void deveBloquearUploadImagemSemAutenticacao() throws Exception {
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem",
                "bolo.png",
                "image/png",
                "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/produtos/1/imagem").file(imagem))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(produtoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarUploadSemArquivo() throws Exception {
        mockMvc.perform(multipart("/api/admin/produtos/1/imagem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisicao invalida"));

        verifyNoInteractions(produtoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveInativarProdutoNaAreaAdministrativa() throws Exception {
        mockMvc.perform(delete("/api/admin/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir(1L);
    }

    private ProdutoRequest produtoRequest(Boolean ativo) {
        return new ProdutoRequest(
                "Bolo de Rolo",
                "Tradicional",
                new BigDecimal("45.90"),
                500,
                "https://example.com/bolo.jpg",
                ativo
        );
    }

    private ProdutoResponse produtoResponse(Boolean ativo) {
        return produtoResponseComImagem("https://example.com/bolo.jpg", ativo);
    }

    private ProdutoResponse produtoResponseComImagem(String imagem) {
        return produtoResponseComImagem(imagem, true);
    }

    private ProdutoResponse produtoResponseComImagem(String imagem, Boolean ativo) {
        return new ProdutoResponse(
                1L,
                "Bolo de Rolo",
                "Tradicional",
                new BigDecimal("45.90"),
                500,
                imagem,
                ativo
        );
    }

    private ProdutoPublicoResponse produtoPublicoResponse() {
        return new ProdutoPublicoResponse(
                1L,
                "Bolo de Rolo",
                new BigDecimal("45.90"),
                500,
                "https://example.com/bolo.jpg",
                7
        );
    }
}
