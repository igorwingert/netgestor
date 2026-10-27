package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.domain.enums.Perfil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

/**
 * Consulta o perfil do usuário da requisição atual.
 * Usado pelos assemblers: um link de ação só aparece se o status permitir
 * E se o perfil do usuário puder executar a ação (ex.: TECNICO não vê "suspender").
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
