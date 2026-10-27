package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Usuario;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import org.springframework.hateoas.server.core.Relation;

/** Dados de saída do usuário. A senha (hash) nunca é devolvida pela API. */
@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        boolean ativo) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo());
    }
}
