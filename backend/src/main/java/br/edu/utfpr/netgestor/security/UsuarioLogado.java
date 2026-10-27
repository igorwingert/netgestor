package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.domain.Usuario;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * O usuário autenticado, no formato que o Spring Security entende (UserDetails).
 *
 * É uma cópia dos dados da entidade Usuario: assim a entidade JPA não fica
 * guardada no contexto de segurança, fora de uma transação.
 * Nos controllers, é obtido com @AuthenticationPrincipal UsuarioLogado usuario.
 */
@Getter
public class UsuarioLogado implements UserDetails {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senha;
    private final Perfil perfil;
    private final boolean ativo;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
    }

    /**
     * O perfil vira uma "authority" com o prefixo ROLE_ (ex.: ROLE_ADMIN).
     * É o que o @PreAuthorize("hasRole('ADMIN')") confere.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    /** No NetGestor o "username" do login é o e-mail. */
    @Override
    public String getUsername() {
        return email;
    }

    /** Usuário desativado (ativo = false) não consegue fazer login nem usar tokens antigos. */
    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
