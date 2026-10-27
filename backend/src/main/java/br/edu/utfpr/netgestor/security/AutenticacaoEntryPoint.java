package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 401 Unauthorized: chamado pelo Spring Security quando uma rota protegida
 * é acessada sem token válido. Devolve o mesmo JSON de erro do resto da API.
 *
 * (O @RestControllerAdvice não serve aqui: o bloqueio acontece no filtro,
 * antes de a requisição chegar a qualquer controller.)
 */
@Component
@RequiredArgsConstructor
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excecao) throws IOException {
        // O JwtAuthFilter guarda o motivo quando o token existe mas é inválido/expirado
        Object motivo = request.getAttribute(JwtAuthFilter.ATRIBUTO_ERRO);
        String mensagem = motivo != null
                ? motivo.toString()
                : "Autenticação necessária: envie o header Authorization: Bearer <token>";

        ErroResponse erro = new ErroResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), mensagem, request.getRequestURI(), List.of());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
