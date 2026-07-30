package br.com.finopaladar.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.finopaladar.backend.dto.LoginRequest;
import br.com.finopaladar.backend.dto.LoginResponse;
import br.com.finopaladar.backend.entity.Administrador;
import br.com.finopaladar.backend.repository.AdministradorRepository;
import br.com.finopaladar.backend.security.JwtClaims;
import br.com.finopaladar.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String JWT_SECRET = "secret-for-tests-with-at-least-32-characters";

    @Mock
    private AdministradorRepository administradorRepository;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService(new ObjectMapper(), JWT_SECRET, 120);
        authService = new AuthService(administradorRepository, passwordEncoder, jwtService);
    }

    @Test
    void deveAutenticarAdministradorAtivoEGerarTokenJwt() {
        Administrador administrador = administrador("admin@finopaladar.com", passwordEncoder.encode("senha123"));
        when(administradorRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@finopaladar.com"))
                .thenReturn(Optional.of(administrador));

        LoginResponse response = authService.login(new LoginRequest("admin@finopaladar.com", "senha123"));

        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.token()).isNotBlank();
        assertThat(response.expiraEm()).isNotNull();
        assertThat(response.administrador().id()).isEqualTo(1L);
        assertThat(response.administrador().nome()).isEqualTo("Admin");
        assertThat(response.administrador().email()).isEqualTo("admin@finopaladar.com");

        JwtClaims claims = jwtService.validarToken(response.token()).orElseThrow();
        assertThat(claims.subject()).isEqualTo("admin@finopaladar.com");
    }

    @Test
    void deveRejeitarSenhaInvalida() {
        Administrador administrador = administrador("admin@finopaladar.com", passwordEncoder.encode("senha123"));
        when(administradorRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@finopaladar.com"))
                .thenReturn(Optional.of(administrador));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@finopaladar.com", "errada")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais invalidas");
    }

    @Test
    void deveRejeitarAdministradorInexistenteOuInativo() {
        when(administradorRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@finopaladar.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@finopaladar.com", "senha123")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais invalidas");
    }

    private Administrador administrador(String email, String senha) {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        administrador.setNome("Admin");
        administrador.setEmail(email);
        administrador.setSenha(senha);
        administrador.setAtivo(true);
        return administrador;
    }
}
