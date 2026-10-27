package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Olt;

public record OltResponse(
        Long id,
        String nome,
        String ipGerencia,
        String fabricante,
        String localizacao,
        Integer totalPortasPon,
        int quantidadeCtos) {

    public static OltResponse de(Olt olt) {
        return new OltResponse(
                olt.getId(),
                olt.getNome(),
                olt.getIpGerencia(),
                olt.getFabricante(),
                olt.getLocalizacao(),
                olt.getTotalPortasPon(),
                olt.getCtos().size());
    }
}
