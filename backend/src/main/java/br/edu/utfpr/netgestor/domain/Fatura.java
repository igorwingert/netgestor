package br.edu.utfpr.netgestor.domain;

import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Fatura mensal de um contrato. */
@Entity
@Table(name = "fatura")
@Getter
@Setter
@NoArgsConstructor
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    /** Mês de referência, sempre no 1º dia (ex.: 2026-10-01). */
    private LocalDate competencia;

    private BigDecimal valor;

    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    private StatusFatura status = StatusFatura.ABERTA;

    /* Lado inverso do 1:N com Pagamento (a FK fatura_id está na tabela pagamento). */
    @OneToMany(mappedBy = "fatura")
    private List<Pagamento> pagamentos = new ArrayList<>();

    /** Soma de todos os pagamentos registrados (aceita pagamentos parciais). */
    public BigDecimal totalPago() {
        return pagamentos.stream()
                .map(Pagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Regra de negócio: a fatura fica PAGA quando a soma dos pagamentos alcança o valor.
     * Enquanto falta pagar, fica VENCIDA (se já passou do vencimento) ou ABERTA.
     * Fatura CANCELADA não muda de status.
     */
    public void atualizarStatus(LocalDate hoje) {
        if (status == StatusFatura.CANCELADA) {
            return;
        }
        if (totalPago().compareTo(valor) >= 0) {
            status = StatusFatura.PAGA;
        } else if (dataVencimento.isBefore(hoje)) {
            status = StatusFatura.VENCIDA;
        } else {
            status = StatusFatura.ABERTA;
        }
    }
}
