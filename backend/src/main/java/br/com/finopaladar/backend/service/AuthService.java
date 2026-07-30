package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.AdministradorAutenticadoResponse;
import br.com.finopaladar.backend.dto.LoginRequest;
import br.com.finopaladar.backend.dto.LoginResponse;
import br.com.finopaladar.backend.entity.Administrador;
import br.com.finopaladar.backend.repository.AdministradorRepository;
import br.com.finopaladar.backend.security.JwtService;
import br.com.finopaladar.backend.security.JwtToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";
    private static final String CREDENCIAIS_INVALIDAS = "Credenciais invalidas";

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim();
        Administrador administrador = administradorRepository.findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

        if (!passwordEncoder.matches(request.senha(), administrador.getSenha())) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }

        JwtToken jwtToken = jwtService.gerarToken(administrador);
        return new LoginResponse(
                jwtToken.token(),
                TIPO_TOKEN,
                jwtToken.expiraEm(),
                new AdministradorAutenticadoResponse(
                        administrador.getId(),
                        administrador.getNome(),
                        administrador.getEmail()
                )
        );
    }
}
