package br.com.finopaladar.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.HistoricoStatusResponse;
import br.com.finopaladar.backend.dto.PedidoItemRequest;
import br.com.finopaladar.backend.dto.PedidoItemResponse;
import br.com.finopaladar.backend.dto.PedidoRequest;
import br.com.finopaladar.backend.dto.PedidoResponse;
import br.com.finopaladar.backend.dto.PedidoStatusRequest;
import br.com.finopaladar.backend.dto.VendaManualRequest;
import br.com.finopaladar.backend.entity.CanalVenda;
import br.com.finopaladar.backend.entity.FormaPagamento;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @Test
    void deveCriarPedidoSemAutenticacao() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pedidos/PED-TESTE"))
                .andExpect(jsonPath("$.codigo").value("PED-TESTE"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"))
                .andExpect(jsonPath("$.valorProdutos").value(91.80))
                .andExpect(jsonPath("$.valorTotal").value(91.80))
                .andExpect(jsonPath("$.itens[0].produtoId").value(1L))
                .andExpect(jsonPath("$.itens[0].subtotal").value(91.80))
                .andExpect(jsonPath("$.mensagem").value("Pedido Fino Paladar\nCodigo: PED-TESTE"))
                .andExpect(jsonPath("$.linkWhatsApp").value("https://wa.me/5583999999999?text=Pedido+Fino+Paladar"));

        verify(pedidoService).criar(any(PedidoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRegistrarVendaManualNaAreaAdministrativa() throws Exception {
        when(pedidoService.registrarVendaManual(any(VendaManualRequest.class))).thenReturn(vendaManualResponse());

        mockMvc.perform(post("/api/admin/pedidos/vendas-manuais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vendaManualRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pedidos/PED-MANUAL"))
                .andExpect(jsonPath("$.codigo").value("PED-MANUAL"))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.canalVenda").value("PRESENCIAL"))
                .andExpect(jsonPath("$.valorTotal").value(91.80));

        verify(pedidoService).registrarVendaManual(any(VendaManualRequest.class));
    }

    @Test
    void deveBloquearRegistroVendaManualSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/admin/pedidos/vendas-manuais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vendaManualRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarCanalVendaManualObrigatorio() throws Exception {
        mockMvc.perform(post("/api/admin/pedidos/vendas-manuais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": "Maria Silva",
                                  "telefone": "83999999999",
                                  "formaPagamento": "PIX",
                                  "tipoRecebimento": "RETIRADA",
                                  "itens": [
                                    {
                                      "produtoId": 1,
                                      "quantidade": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'canalVenda')]").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveBuscarPedidoPorCodigoSemAutenticacao() throws Exception {
        when(pedidoService.buscarPorCodigo("PED-TESTE")).thenReturn(response());

        mockMvc.perform(get("/api/pedidos/PED-TESTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PED-TESTE"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"));

        verify(pedidoService).buscarPorCodigo("PED-TESTE");
    }

    @Test
    void deveListarHistoricoPorCodigoSemAutenticacao() throws Exception {
        Instant dataHora = Instant.parse("2026-08-01T10:15:00Z");
        when(pedidoService.listarHistoricoPorCodigo("PED-TESTE"))
                .thenReturn(List.of(new HistoricoStatusResponse(
                        1L,
                        StatusPedido.AGUARDANDO_CONFIRMACAO,
                        dataHora
                )));

        mockMvc.perform(get("/api/pedidos/PED-TESTE/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AGUARDANDO_CONFIRMACAO"))
                .andExpect(jsonPath("$[0].dataHora").value("2026-08-01T10:15:00Z"));

        verify(pedidoService).listarHistoricoPorCodigo("PED-TESTE");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAlterarStatusNaAreaAdministrativa() throws Exception {
        PedidoStatusRequest request = new PedidoStatusRequest(StatusPedido.ACEITO);

        mockMvc.perform(patch("/api/admin/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(pedidoService).alterarStatus(eq(1L), any(PedidoStatusRequest.class));
    }

    @Test
    void deveBloquearAlteracaoStatusSemAutenticacao() throws Exception {
        PedidoStatusRequest request = new PedidoStatusRequest(StatusPedido.ACEITO);

        mockMvc.perform(patch("/api/admin/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarStatusObrigatorioNaAlteracao() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'status')]").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveValidarPedidoSemItens() throws Exception {
        PedidoRequest request = new PedidoRequest(
                "Maria Silva",
                "83999999999",
                FormaPagamento.PIX,
                TipoRecebimento.RETIRADA,
                null,
                null,
                List.of()
        );

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'itens')]").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRejeitarFormaPagamentoInvalida() throws Exception {
        String request = """
                {
                  "cliente": "Maria Silva",
                  "telefone": "83999999999",
                  "formaPagamento": "CARTAO",
                  "tipoRecebimento": "RETIRADA",
                  "itens": [
                    {
                      "produtoId": 1,
                      "quantidade": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisicao invalida"));

        verifyNoInteractions(pedidoService);
    }

    private PedidoRequest request() {
        return new PedidoRequest(
                "Maria Silva",
                "83999999999",
                FormaPagamento.PIX,
                TipoRecebimento.RETIRADA,
                "Sem observacoes",
                null,
                List.of(new PedidoItemRequest(1L, 2))
        );
    }

    private VendaManualRequest vendaManualRequest() {
        return new VendaManualRequest(
                CanalVenda.PRESENCIAL,
                "Maria Silva",
                "83999999999",
                FormaPagamento.PIX,
                TipoRecebimento.RETIRADA,
                "Venda registrada no balcao",
                null,
                List.of(new PedidoItemRequest(1L, 2))
        );
    }

    private PedidoResponse response() {
        return new PedidoResponse(
                1L,
                "PED-TESTE",
                "Maria Silva",
                "83999999999",
                StatusPedido.AGUARDANDO_CONFIRMACAO,
                FormaPagamento.PIX,
                TipoRecebimento.RETIRADA,
                null,
                new BigDecimal("91.80"),
                BigDecimal.ZERO,
                new BigDecimal("91.80"),
                "Sem observacoes",
                LocalDate.of(2026, 8, 1),
                null,
                List.of(new PedidoItemResponse(
                        1L,
                        1L,
                        "Bolo de Rolo",
                        new BigDecimal("45.90"),
                        500,
                        2,
                        new BigDecimal("91.80")
                )),
                "Pedido Fino Paladar\nCodigo: PED-TESTE",
                "https://wa.me/5583999999999?text=Pedido+Fino+Paladar"
        );
    }

    private PedidoResponse vendaManualResponse() {
        return new PedidoResponse(
                2L,
                "PED-MANUAL",
                "Maria Silva",
                "83999999999",
                StatusPedido.ENTREGUE,
                FormaPagamento.PIX,
                TipoRecebimento.RETIRADA,
                CanalVenda.PRESENCIAL,
                new BigDecimal("91.80"),
                BigDecimal.ZERO,
                new BigDecimal("91.80"),
                "Venda registrada no balcao",
                LocalDate.of(2026, 8, 1),
                null,
                List.of(new PedidoItemResponse(
                        1L,
                        1L,
                        "Bolo de Rolo",
                        new BigDecimal("45.90"),
                        500,
                        2,
                        new BigDecimal("91.80")
                )),
                null,
                null
        );
    }
}
