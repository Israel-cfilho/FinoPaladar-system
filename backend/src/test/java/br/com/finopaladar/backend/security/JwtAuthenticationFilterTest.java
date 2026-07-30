package br.com.finopaladar.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.entity.Administrador;
import br.com.finopaladar.backend.repository.AdministradorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String JWT_SECRET = "secret-for-tests-with-at-least-32-characters";

    @Mock
    private AdministradorRepository administradorRepository;

    private JwtService jwtService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new ObjectMapper(), JWT_SECRET, 120);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, administradorRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarAdministradorComTokenValido() throws Exception {
        Administrador administrador = administrador();
        String token = jwtService.gerarToken(administrador).token();
        when(administradorRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@finopaladar.com"))
                .thenReturn(Optional.of(administrador));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvocado = new AtomicBoolean(false);

        jwtAuthenticationFilter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainInvocado.set(true)
        );

        assertThat(chainInvocado.get()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("admin@finopaladar.com");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void deveIgnorarTokenInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvocado = new AtomicBoolean(false);

        jwtAuthenticationFilter.doFilter(request, response, (servletRequest, servletResponse) ->
                chainInvocado.set(true)
        );

        assertThat(chainInvocado.get()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(administradorRepository);
    }

    private Administrador administrador() {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        administrador.setNome("Admin");
        administrador.setEmail("admin@finopaladar.com");
        administrador.setSenha("senha");
        administrador.setAtivo(true);
        return administrador;
    }
}
