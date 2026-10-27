package br.edu.utfpr.netgestor.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança.
 *
 * Duas camadas de proteção:
 * 1. Aqui (filtros): toda rota /api exige token, exceto o login. Sem token -> 401.
 * 2. Nos controllers (@PreAuthorize): cada operação define quais perfis podem executá-la.
 *    Perfil sem permissão -> 403. Isso é ligado pelo @EnableMethodSecurity.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final AutenticacaoEntryPoint autenticacaoEntryPoint;
    private final AcessoNegadoHandler acessoNegadoHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST sem sessão nem cookies: a proteção CSRF não se aplica
                .csrf(csrf -> csrf.disable())
                // usa o CorsConfigurationSource definido em config/CorsConfig
                .cors(Customizer.withDefaults())
                // STATELESS: o servidor não guarda sessão; cada requisição traz o próprio token
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // respostas 401 e 403 no mesmo formato JSON do resto da API
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(autenticacaoEntryPoint)
                        .accessDeniedHandler(acessoNegadoHandler))
                // o filtro JWT roda antes do filtro padrão de login por usuário/senha
                .addFilterBefore(new JwtAuthFilter(jwtService, usuarioDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Usado no login: confere e-mail e senha com o UsuarioDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    /** Algoritmo de hash das senhas. O mesmo usado para gerar os hashes da migration V2. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
