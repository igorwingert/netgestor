package br.edu.utfpr.netgestor.domain;

import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** ONU ou roteador. fica em estoque ou instalado em comodato num contrato. */
@Entity
@Table(name = "equipamento")
@Getter
@Setter
@NoArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoEquipamento tipo;

    private String fabricante;

    private String modelo;

    private String numeroSerie;

    /** endereco MAC no formato AA:BB:CC:DD:EE:FF. */
    private String mac;

    @Enumerated(EnumType.STRING)
    private StatusEquipamento status = StatusEquipamento.ESTOQUE;

    /** Contrato onde o equipamento esta instalado (vazio quando em estoque). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;
}
