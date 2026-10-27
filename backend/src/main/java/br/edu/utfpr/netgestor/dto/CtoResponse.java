package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Cto;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "ctos", itemRelation = "cto")
public record CtoResponse(
        Long id,
        String identificacao,
        Long oltId,
        String oltNome,
        Integer portaPon,
        Integer capacidade,
        BigDecimal latitude,
        BigDecimal longitude) {

    public static CtoResponse de(Cto cto) {
        return new CtoResponse(
                cto.getId(),
                cto.getIdentificacao(),
                cto.getOlt().getId(),
                cto.getOlt().getNome(),
                cto.getPortaPon(),
                cto.getCapacidade(),
                cto.getLatitude(),
                cto.getLongitude());
    }
}
