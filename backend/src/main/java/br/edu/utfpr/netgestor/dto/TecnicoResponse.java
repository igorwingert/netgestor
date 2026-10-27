package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Tecnico;

public record TecnicoResponse(
        Long id,
        String nome,
        String telefone,
        String especialidade,
        Long usuarioId,
        String usuarioEmail) {

    public static TecnicoResponse de(Tecnico tecnico) {
        return new TecnicoResponse(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getTelefone(),
                tecnico.getEspecialidade(),
                tecnico.getUsuario().getId(),
                tecnico.getUsuario().getEmail());
    }
}
