package br.com.finopaladar.backend.security;

import br.com.finopaladar.backend.repository.AdministradorRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectProvider<JwtService> jwtService;
    private final ObjectProvider<AdministradorRepository> administradorRepository;

    public SecurityConfig(
            ObjectProvider<JwtService> jwtService,
            ObjectProvider<AdministradorRepository> administradorRepository
    ) {
        this.jwtService = jwtService;
        this.administradorRepository = administradorRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/produtos/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        JwtService jwtServiceBean = jwtService.getIfAvailable();
        AdministradorRepository administradorRepositoryBean = administradorRepository.getIfAvailable();
        if (jwtServiceBean != null && administradorRepositoryBean != null) {
            http.addFilterBefore(
                    new JwtAuthenticationFilter(jwtServiceBean, administradorRepositoryBean),
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
