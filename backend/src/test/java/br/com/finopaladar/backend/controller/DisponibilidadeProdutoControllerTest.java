package br.com.finopaladar.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.DisponibilidadeProdutoRequest;
import br.com.finopaladar.backend.dto.DisponibilidadeProdutoResponse;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.DisponibilidadeProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DisponibilidadeProdutoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DisponibilidadeProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DisponibilidadeProdutoService disponibilidadeProdutoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCadastrarDisponibilidade() throws Exception {
        when(disponibilidadeProdutoService.cadastrar(any(DisponibilidadeProdutoRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/admin/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/admin/disponibilidade/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.produtoId").value(1L))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));
    }

    @Test
    void deveBloquearCadastroSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/admin/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(disponibilidadeProdutoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarQuantidadeNegativa() throws Exception {
        DisponibilidadeProdutoRequest request = new DisponibilidadeProdutoRequest(
                1L,
                -1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2)
        );

        mockMvc.perform(post("/api/admin/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'quantidadeDisponivel')]").exists());

        verifyNoInteractions(disponibilidadeProdutoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarDataFinalMaiorQueInicial() throws Exception {
        DisponibilidadeProdutoRequest request = new DisponibilidadeProdutoRequest(
                1L,
                10,
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 2)
        );

        mockMvc.perform(post("/api/admin/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'dataFinal')]").exists());

        verifyNoInteractions(disponibilidadeProdutoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarDisponibilidades() throws Exception {
        when(disponibilidadeProdutoService.consultar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/admin/disponibilidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].produtoNome").value("Bolo de Rolo"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarDisponibilidadePorId() throws Exception {
        when(disponibilidadeProdutoService.consultarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/admin/disponibilidade/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.produtoId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveEditarDisponibilidade() throws Exception {
        when(disponibilidadeProdutoService.editar(eq(1L), any(DisponibilidadeProdutoRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/admin/disponibilidade/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveExcluirDisponibilidade() throws Exception {
        mockMvc.perform(delete("/api/admin/disponibilidade/1"))
                .andExpect(status().isNoContent());

        verify(disponibilidadeProdutoService).excluir(1L);
    }

    private DisponibilidadeProdutoRequest request() {
        return new DisponibilidadeProdutoRequest(
                1L,
                10,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2)
        );
    }

    private DisponibilidadeProdutoResponse response() {
        return new DisponibilidadeProdutoResponse(
                1L,
                1L,
                "Bolo de Rolo",
                10,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2)
        );
    }
}
