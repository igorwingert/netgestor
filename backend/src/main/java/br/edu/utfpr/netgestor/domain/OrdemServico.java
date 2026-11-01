package br.edu.utfpr.netgestor.domain;

import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import br.edu.utfpr.netgestor.domain.enums.TipoOrdemServico;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** ordem de servico: trabalho de campo executado por um tecnico num contrato. */
@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@NoArgsConstructor
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    /** opcional: uma instalacao, por exemplo, nao nasce de um chamado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @Enumerated(EnumType.STRING)
    private TipoOrdemServico tipo;

    @Enumerated(EnumType.STRING)
    private StatusOrdemServico status = StatusOrdemServico.AGENDADA;

    private LocalDateTime dataAgendada;

    private LocalDateTime dataConclusao;

    private String observacoes;

    /*
     * relacionamento N:N: uma OS usa varios equipamentos e um equipamento aparece
     * em varias OS ao longo da vida. o JPA usa a tabela de juncao abaixo.
     * Set (e nao List) evita duplicar o mesmo equipamento na mesma OS.
     */
    @ManyToMany
    @JoinTable(
            name = "ordem_servico_equipamento",
            joinColumns = @JoinColumn(name = "ordem_servico_id"),
            inverseJoinColumns = @JoinColumn(name = "equipamento_id"))
    private Set<Equipamento> equipamentos = new HashSet<>();
}
