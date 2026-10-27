package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Fatura;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Relation(collectionRelation = "faturas", itemRelation = "fatura")
public record FaturaResponse(
        Long id,
        Long contratoId,
        String contratoNumero,
        String clienteNome,
        YearMonth competencia,
        BigDecimal valor,
        BigDecimal valorPago,
        LocalDate dataVencimento,
        StatusFatura status) {

    public static FaturaResponse de(Fatura fatura) {
        return new FaturaResponse(
                fatura.getId(),
                fatura.getContrato().getId(),
                fatura.getContrato().getNumero(),
                fatura.getContrato().getCliente().getNome(),
                YearMonth.from(fatura.getCompetencia()),
                fatura.getValor(),
                fatura.totalPago(),
                fatura.getDataVencimento(),
                fatura.getStatus());
    }
}
