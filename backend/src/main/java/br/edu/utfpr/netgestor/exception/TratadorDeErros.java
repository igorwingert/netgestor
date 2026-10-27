package br.edu.utfpr.netgestor.exception;

import br.edu.utfpr.netgestor.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Centraliza o tratamento de erros de todos os controllers.
 * Cada exceção é convertida no mesmo formato JSON (ErroResponse) com o status HTTP adequado,
 * assim os controllers e services não precisam montar respostas de erro.
 */
@Slf4j
@RestControllerAdvice
public class TratadorDeErros {

    /** 404: id não encontrado. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> recursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    /** 409: regra de negócio violada. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    /** 401: login com e-mail/senha errados ou usuário desativado. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> autenticacao(AuthenticationException ex, HttpServletRequest request) {
        String mensagem = ex instanceof DisabledException
                ? "Usuário desativado"
                : "E-mail ou senha inválidos";
        return resposta(HttpStatus.UNAUTHORIZED, mensagem, request, List.of());
    }

    /**
     * 403: o @PreAuthorize (ou uma regra do service) negou o acesso.
     * Sem este handler, a exceção cairia no handler genérico e viraria 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        String mensagem = ex.getMessage() != null && !ex.getMessage().startsWith("Access Denied")
                ? ex.getMessage()
                : "Seu perfil não tem permissão para esta operação";
        return resposta(HttpStatus.FORBIDDEN, mensagem, request, List.of());
    }

    /** 400: Bean Validation (@NotBlank, @Size...) falhou. Lista o erro de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroResponse.Campo> campos = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.add(new ErroResponse.Campo(erro.getField(), erro.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(erro ->
                campos.add(new ErroResponse.Campo(erro.getObjectName(), erro.getDefaultMessage())));
        return resposta(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição", request, campos);
    }

    /** 400: JSON malformado ou valor de tipo errado (ex.: enum inexistente, data inválida). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Corpo da requisição inválido: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o formato do JSON e os valores dos campos",
                request, List.of());
    }

    /** 400: parâmetro de URL com tipo errado (ex.: /api/clientes/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                          HttpServletRequest request) {
        String mensagem = "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue();
        return resposta(HttpStatus.BAD_REQUEST, mensagem, request, List.of());
    }

    /** 404: URL que não corresponde a nenhum endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Endpoint não encontrado", request, List.of());
    }

    /** 405: verbo HTTP não suportado na rota (ex.: DELETE /api/clientes). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest request) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " não é suportado nesta rota", request, List.of());
    }

    /**
     * 409: o banco recusou a operação por uma constraint (UNIQUE, FK, CHECK).
     * Os services já validam as regras antes; isto é uma rede de segurança
     * (ex.: duas requisições simultâneas cadastrando o mesmo CPF).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT,
                "A operação viola uma restrição do banco de dados (registro duplicado ou em uso)",
                request, List.of());
    }

    /** 500: qualquer erro inesperado. O detalhe vai para o log, não para o cliente da API. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", request, List.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem,
                                                  HttpServletRequest request, List<ErroResponse.Campo> campos) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
