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
 * filtro executado uma vez em cada requisicao, antes dos controllers.
 *
 * 1. le o header "Authorization: Bearer <token>".
 * 2. valida o token (assinatura e expiracao) e descobre o e-mail do usuario.
 * 3. carrega o usuario do banco e o coloca no SecurityContext: a partir dai,
 *    para o Spring, a requisicao esta autenticada com o perfil daquele usuario.
 *
 * se nao houver token ou ele for invalido, o filtro apenas segue adiante sem autenticar.
 * quem devolve o 401 e o AutenticacaoEntryPoint, quando a rota exige login.
 *
 * nao e um @Component de proposito: e criado no SecurityConfig, para nao ser
 * registrado duas vezes (uma pelo Spring Boot e outra pelo Spring Security).
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** atributo da requisicao com o motivo da falha, lido pelo AutenticacaoEntryPoint. */
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
            // busca o usuario a cada requisicao: se ele for desativado ou mudar de perfil,
            // o efeito e imediato, mesmo com um token ainda dentro da validade.
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
