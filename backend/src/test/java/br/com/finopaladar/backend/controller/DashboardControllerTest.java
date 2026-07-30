package br.com.finopaladar.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.DashboardResumoResponse;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.DashboardService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarResumoDashboardNaAreaAdministrativa() throws Exception {
        when(dashboardService.buscarResumo()).thenReturn(new DashboardResumoResponse(
                6L,
                4L,
                10L,
                2L,
                new BigDecimal("250.50")
        ));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidosHoje").value(6L))
                .andExpect(jsonPath("$.pedidosEmAberto").value(4L))
                .andExpect(jsonPath("$.pedidosEntregues").value(10L))
                .andExpect(jsonPath("$.pedidosCancelados").value(2L))
                .andExpect(jsonPath("$.valorVendidoHoje").value(250.50));

        verify(dashboardService).buscarResumo();
    }

    @Test
    void deveBloquearDashboardSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dashboardService);
    }
}
