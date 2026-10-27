package br.edu.utfpr.netgestor.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro executado UMA vez em cada requisição, antes dos controllers.
 *
 * 1. Lê o header "Authorization: Bearer <token>".
 * 2. Valida o token (assinatura e expiração) e descobre o e-mail do usuário.
 * 3. Carrega o usuário do banco e o coloca no SecurityContext: a partir daí,
 *    para o Spring, a requisição está autenticada com o perfil daquele usuário.
 *
 * Se não houver token ou ele for inválido, o filtro apenas segue adiante sem autenticar.
 * Quem devolve o 401 é o AutenticacaoEntryPoint, quando a rota exige login.
 *
 * Não é um @Component de propósito: é criado no SecurityConfig, para não ser
 * registrado duas vezes (uma pelo Spring Boot e outra pelo Spring Security).
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Atributo da requisição com o motivo da falha, lido pelo AutenticacaoEntryPoint. */
    public static final String ATRIBUTO_ERRO = "netgestor.erroAutenticacao";

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIXO)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIXO.length());
        try {
            String email = jwtService.extrairEmail(token);
            // Busca o usuário a cada requisição: se ele for desativado ou mudar de perfil,
            // o efeito é imediato, mesmo com um token ainda dentro da validade.
            UsuarioLogado usuario = usuarioDetailsService.loadUserByUsername(email);
            if (usuario.isEnabled()) {
                var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(autenticacao);
                SecurityContextHolder.setContext(contexto);
            } else {
                request.setAttribute(ATRIBUTO_ERRO, "Usuário desativado");
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(ATRIBUTO_ERRO, "Token expirado: faça login novamente");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(ATRIBUTO_ERRO, "Token inválido");
        } catch (UsernameNotFoundException e) {
            request.setAttribute(ATRIBUTO_ERRO, "O usuário do token não existe mais");
        }

        chain.doFilter(request, response);
    }
}
