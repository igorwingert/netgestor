package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 403 Forbidden no nível dos filtros: o usuário está autenticado, mas o perfil dele
 * não tem acesso. (Os 403 do @PreAuthorize, que acontecem já dentro do controller,
 * são tratados pelo TratadorDeErros com o mesmo formato.)
 */
@Component
@RequiredArgsConstructor
public class AcessoNegadoHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excecao) throws IOException {
        ErroResponse erro = new ErroResponse(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(), "Seu perfil não tem permissão para acessar este recurso",
                request.getRequestURI(), List.of());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
