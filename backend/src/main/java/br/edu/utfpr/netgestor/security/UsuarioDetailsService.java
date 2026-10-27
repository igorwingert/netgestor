package br.edu.utfpr.netgestor.security;

import br.edu.utfpr.netgestor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensina o Spring Security a buscar um usuário no banco pelo e-mail.
 * É usado em dois momentos: no login (para conferir a senha) e
 * em cada requisição com token (para carregar o usuário dono do token).
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UsuarioLogado loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .map(UsuarioLogado::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
