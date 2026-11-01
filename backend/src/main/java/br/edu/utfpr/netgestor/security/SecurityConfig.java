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
 * configuracao central de seguranca.
 *
 * duas camadas de protecao:
 * 1. aqui (filtros): toda rota /api exige token, exceto o login. sem token -> 401.
 * 2. nos controllers (@PreAuthorize): cada operacao define quais perfis podem executa-la.
 *    Perfil sem permissao -> 403. isso e ligado pelo @EnableMethodSecurity.
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
                // API REST sem sessao nem cookies: a protecao CSRF nao se aplica
                .csrf(csrf -> csrf.disable())
                // usa o CorsConfigurationSource definido em config/CorsConfig
                .cors(Customizer.withDefaults())
                // STATELESS: o servidor nao guarda sessao; cada requisicao traz o proprio token
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // respostas 401 e 403 no mesmo formato JSON do resto da API
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(autenticacaoEntryPoint)
                        .accessDeniedHandler(acessoNegadoHandler))
                // o filtro JWT roda antes do filtro padrao de login por usuario/senha
                .addFilterBefore(new JwtAuthFilter(jwtService, usuarioDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * usado no login: confere e-mail e senha com o UsuarioDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    /** algoritmo de hash das senhas. o mesmo usado para gerar os hashes da migration V2. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
