package br.com.finopaladar.backend.security;

import br.com.finopaladar.backend.entity.Administrador;
import br.com.finopaladar.backend.repository.AdministradorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AdministradorRepository administradorRepository;

    public JwtAuthenticationFilter(JwtService jwtService, AdministradorRepository administradorRepository) {
        this.jwtService = jwtService;
        this.administradorRepository = administradorRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extrairToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validarToken(token)
                    .flatMap(claims -> administradorRepository.findByEmailIgnoreCaseAndAtivoTrue(claims.subject()))
                    .ifPresent(administrador -> autenticarAdministrador(request, administrador));
        }

        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private void autenticarAdministrador(HttpServletRequest request, Administrador administrador) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                administrador.getEmail(),
                null,
                authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
