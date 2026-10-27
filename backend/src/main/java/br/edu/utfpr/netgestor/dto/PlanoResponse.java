package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Plano;
import br.edu.utfpr.netgestor.domain.enums.Tecnologia;

import java.math.BigDecimal;

public record PlanoResponse(
        Long id,
        String nome,
        Integer downloadMbps,
        Integer uploadMbps,
        BigDecimal valorMensal,
        Tecnologia tecnologia,
        boolean ativo) {

    public static PlanoResponse de(Plano plano) {
        return new PlanoResponse(
                plano.getId(),
                plano.getNome(),
                plano.getDownloadMbps(),
                plano.getUploadMbps(),
                plano.getValorMensal(),
                plano.getTecnologia(),
                plano.isAtivo());
    }
}
