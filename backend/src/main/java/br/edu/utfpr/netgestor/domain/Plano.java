package br.edu.utfpr.netgestor.domain;

import br.edu.utfpr.netgestor.domain.enums.Tecnologia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Plano de internet vendido pelo provedor. */
@Entity
@Table(name = "plano")
@Getter
@Setter
@NoArgsConstructor
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer downloadMbps;

    private Integer uploadMbps;

    /** BigDecimal para dinheiro: double tem erros de arredondamento. */
    private BigDecimal valorMensal;

    @Enumerated(EnumType.STRING)
    private Tecnologia tecnologia;

    private boolean ativo = true;
}
