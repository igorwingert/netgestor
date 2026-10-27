package br.edu.utfpr.netgestor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS: o navegador só deixa o Angular (http://localhost:4200) chamar a API
 * (http://localhost:8080) se a API autorizar essa outra origem explicitamente.
 * O SecurityConfig usa este bean em .cors(...).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${netgestor.cors.allowed-origins}") List<String> origensPermitidas) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origensPermitidas);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        // Permite que o JavaScript do frontend leia o header Location das respostas 201
        config.setExposedHeaders(List.of(HttpHeaders.LOCATION));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
