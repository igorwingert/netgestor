package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.domain.enums.Perfil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

/**
 * consulta o perfil do usuario da requisicao atual.
 * usado pelos assemblers: um link de acao so aparece se o status permitir
 * e se o perfil do usuario puder executar a acao (ex.: TECNICO nao ve "suspender").
 */
public final class Permissoes {

    private Permissoes() {
    }

    public static boolean usuarioTemPerfil(Perfil... perfis) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioLogado usuario)) {
            return false;
        }
        return Arrays.asList(perfis).contains(usuario.getPerfil());
    }
}
