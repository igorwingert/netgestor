package br.edu.utfpr.netgestor.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * CTO (Caixa de Terminação Óptica): caixa instalada no poste que distribui
 * a fibra para os clientes. Cada contrato de fibra ocupa uma porta da CTO.
 */
@Entity
@Table(name = "cto")
@Getter
@Setter
@NoArgsConstructor
public class Cto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identificacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "olt_id")
    private Olt olt;

    /** Porta PON da OLT que alimenta esta CTO. */
    private Integer portaPon;

    /** Quantidade de portas disponíveis para clientes. */
    private Integer capacidade;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
