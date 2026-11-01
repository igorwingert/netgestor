package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.dto.LoginRequest;
import br.edu.utfpr.netgestor.dto.LoginResponse;
import br.edu.utfpr.netgestor.dto.UsuarioResponse;
import br.edu.utfpr.netgestor.security.JwtService;
import br.edu.utfpr.netgestor.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    /**
     * o AuthenticationManager busca o usuario pelo e-mail (UsuarioDetailsService) e compara a senha
     * informada com o hash BCrypt do banco. se nao conferir, lanca BadCredentialsException,
     * que o TratadorDeErros transforma em 401. se conferir, geramos o token.
     */
    public LoginResponse login(LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.senha()));

        UsuarioLogado usuario = (UsuarioLogado) autenticacao.getPrincipal();
        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.getExpiracaoSegundos(),
                usuarioService.buscarPorId(usuario.getId()));
    }

    /** dados do usuario dono do token (GET /api/auth/me). */
    public UsuarioResponse usuarioAtual(UsuarioLogado usuario) {
        return usuarioService.buscarPorId(usuario.getId());
    }
}
