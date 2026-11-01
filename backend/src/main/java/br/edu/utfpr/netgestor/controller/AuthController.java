package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.LoginRequest;
import br.edu.utfpr.netgestor.dto.LoginResponse;
import br.edu.utfpr.netgestor.dto.UsuarioResponse;
import br.edu.utfpr.netgestor.security.UsuarioLogado;
import br.edu.utfpr.netgestor.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * autenticacao:
 *   POST /api/auth/login   publico: recebe e-mail/senha e devolve o token JWT (200) ou 401
 *   GET  /api/auth/me      exige token: devolve os dados do usuario logado
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacaoService autenticacaoService;

    /** 200 e nao 201: o login nao cria um recurso, apenas emite um token. */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return autenticacaoService.login(request);
    }

    /** @AuthenticationPrincipal injeta o usuario que o JwtAuthFilter colocou no SecurityContext. */
    @GetMapping("/me")
    public EntityModel<UsuarioResponse> me(@AuthenticationPrincipal UsuarioLogado usuario) {
        return EntityModel.of(autenticacaoService.usuarioAtual(usuario),
                linkTo(methodOn(AuthController.class).me(null)).withSelfRel());
    }
}
