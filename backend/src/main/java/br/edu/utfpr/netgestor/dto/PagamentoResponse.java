package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Pagamento;
import br.edu.utfpr.netgestor.domain.enums.FormaPagamento;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Relation(collectionRelation = "pagamentos", itemRelation = "pagamento")
public record PagamentoResponse(
        Long id,
        Long faturaId,
        LocalDate dataPagamento,
        BigDecimal valorPago,
        FormaPagamento forma) {

    public static PagamentoResponse de(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getFatura().getId(),
                pagamento.getDataPagamento(),
                pagamento.getValorPago(),
                pagamento.getForma());
    }
}
