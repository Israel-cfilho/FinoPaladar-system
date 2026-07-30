package br.com.finopaladar.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.finopaladar.backend.dto.AdministradorAutenticadoResponse;
import br.com.finopaladar.backend.dto.LoginRequest;
import br.com.finopaladar.backend.dto.LoginResponse;
import br.com.finopaladar.backend.exception.GlobalExceptionHandler;
import br.com.finopaladar.backend.security.SecurityConfig;
import br.com.finopaladar.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void deveRealizarLoginSemAutenticacao() throws Exception {
        LoginRequest request = new LoginRequest("admin@finopaladar.com", "senha123");
        when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResponse(
                "jwt-token",
                "Bearer",
                Instant.parse("2026-07-29T12:00:00Z"),
                new AdministradorAutenticadoResponse(1L, "Admin", "admin@finopaladar.com")
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").value("2026-07-29T12:00:00Z"))
                .andExpect(jsonPath("$.administrador.id").value(1L))
                .andExpect(jsonPath("$.administrador.email").value("admin@finopaladar.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void deveValidarLoginInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'senha')]").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarUnauthorizedParaCredenciaisInvalidas() throws Exception {
        LoginRequest request = new LoginRequest("admin@finopaladar.com", "errada");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciais invalidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"));
    }
}
