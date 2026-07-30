package br.com.finopaladar.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse;
import br.com.finopaladar.backend.dto.ProdutoVendidoResponse;
import br.com.finopaladar.backend.dto.RelatorioFaturamentoResponse;
import br.com.finopaladar.backend.dto.TicketMedioResponse;
import br.com.finopaladar.backend.entity.Cidade;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.RelatorioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RelatorioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelatorioService relatorioService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarFaturamentoDiario() throws Exception {
        LocalDate data = LocalDate.of(2026, 7, 29);
        when(relatorioService.faturamentoDiario(data)).thenReturn(faturamento(data, data, "150.00"));

        mockMvc.perform(get("/api/admin/relatorios/faturamento/diario")
                        .param("data", "2026-07-29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-07-29"))
                .andExpect(jsonPath("$.dataFinal").value("2026-07-29"))
                .andExpect(jsonPath("$.valorTotal").value(150.00));

        verify(relatorioService).faturamentoDiario(data);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarFaturamentoSemanal() throws Exception {
        LocalDate data = LocalDate.of(2026, 7, 29);
        when(relatorioService.faturamentoSemanal(data))
                .thenReturn(faturamento(LocalDate.of(2026, 7, 27), LocalDate.of(2026, 8, 2), "700.00"));

        mockMvc.perform(get("/api/admin/relatorios/faturamento/semanal")
                        .param("data", "2026-07-29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-07-27"))
                .andExpect(jsonPath("$.dataFinal").value("2026-08-02"))
                .andExpect(jsonPath("$.valorTotal").value(700.00));

        verify(relatorioService).faturamentoSemanal(data);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarFaturamentoMensal() throws Exception {
        when(relatorioService.faturamentoMensal(2026, 7))
                .thenReturn(faturamento(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), "3200.00"));

        mockMvc.perform(get("/api/admin/relatorios/faturamento/mensal")
                        .param("ano", "2026")
                        .param("mes", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-07-01"))
                .andExpect(jsonPath("$.dataFinal").value("2026-07-31"))
                .andExpect(jsonPath("$.valorTotal").value(3200.00));

        verify(relatorioService).faturamentoMensal(2026, 7);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarFaturamentoAnual() throws Exception {
        when(relatorioService.faturamentoAnual(2026))
                .thenReturn(faturamento(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "25000.00"));

        mockMvc.perform(get("/api/admin/relatorios/faturamento/anual")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-01-01"))
                .andExpect(jsonPath("$.dataFinal").value("2026-12-31"))
                .andExpect(jsonPath("$.valorTotal").value(25000.00));

        verify(relatorioService).faturamentoAnual(2026);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarFaturamentoPersonalizado() throws Exception {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioService.faturamentoPorPeriodo(dataInicial, dataFinal))
                .thenReturn(faturamento(dataInicial, dataFinal, "3200.00"));

        mockMvc.perform(get("/api/admin/relatorios/faturamento")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-07-01"))
                .andExpect(jsonPath("$.dataFinal").value("2026-07-31"))
                .andExpect(jsonPath("$.valorTotal").value(3200.00));

        verify(relatorioService).faturamentoPorPeriodo(dataInicial, dataFinal);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarProdutosMaisVendidos() throws Exception {
        ProdutoVendidoResponse produto = produtoVendido();
        when(relatorioService.produtosMaisVendidos(any(LocalDate.class), any(LocalDate.class), eq(5)))
                .thenReturn(List.of(produto));

        mockMvc.perform(get("/api/admin/relatorios/produtos-mais-vendidos")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31")
                        .param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").value(1L))
                .andExpect(jsonPath("$[0].nomeProduto").value("Bolo de Rolo"))
                .andExpect(jsonPath("$[0].quantidadeVendida").value(8L))
                .andExpect(jsonPath("$[0].valorVendido").value(367.20));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarQuantidadeVendidaPorProduto() throws Exception {
        ProdutoVendidoResponse produto = produtoVendido();
        when(relatorioService.quantidadeVendidaPorProduto(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(produto));

        mockMvc.perform(get("/api/admin/relatorios/produtos/quantidade-vendida")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").value(1L))
                .andExpect(jsonPath("$[0].quantidadeVendida").value(8L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarTicketMedio() throws Exception {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioService.ticketMedio(dataInicial, dataFinal))
                .thenReturn(new TicketMedioResponse(dataInicial, dataFinal, new BigDecimal("100.33")));

        mockMvc.perform(get("/api/admin/relatorios/ticket-medio")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-07-01"))
                .andExpect(jsonPath("$.dataFinal").value("2026-07-31"))
                .andExpect(jsonPath("$.valor").value(100.33));

        verify(relatorioService).ticketMedio(dataInicial, dataFinal);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveConsultarCidadeComMaisPedidos() throws Exception {
        LocalDate dataInicial = LocalDate.of(2026, 7, 1);
        LocalDate dataFinal = LocalDate.of(2026, 7, 31);
        when(relatorioService.cidadeComMaisPedidos(dataInicial, dataFinal))
                .thenReturn(new CidadeMaisPedidosResponse(Cidade.BANANEIRAS, 12L));

        mockMvc.perform(get("/api/admin/relatorios/cidade-mais-pedidos")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("BANANEIRAS"))
                .andExpect(jsonPath("$.quantidadePedidos").value(12L));

        verify(relatorioService).cidadeComMaisPedidos(dataInicial, dataFinal);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveValidarLimiteProdutosMaisVendidos() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/produtos-mais-vendidos")
                        .param("dataInicial", "2026-07-01")
                        .param("dataFinal", "2026-07-31")
                        .param("limite", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(relatorioService);
    }

    @Test
    void deveBloquearRelatoriosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/faturamento/diario")
                        .param("data", "2026-07-29"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(relatorioService);
    }

    private RelatorioFaturamentoResponse faturamento(
            LocalDate dataInicial,
            LocalDate dataFinal,
            String valorTotal
    ) {
        return new RelatorioFaturamentoResponse(dataInicial, dataFinal, new BigDecimal(valorTotal));
    }

    private ProdutoVendidoResponse produtoVendido() {
        return new ProdutoVendidoResponse(
                1L,
                "Bolo de Rolo",
                8L,
                new BigDecimal("367.20")
        );
    }
}
