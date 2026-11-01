package br.edu.utfpr.netgestor.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * OLT (Optical Line Terminal): equipamento central da rede de fibra,
 * instalado no POP do provedor. cada porta PON alimenta uma ou mais CTOs.
 */
@Entity
@Table(name = "olt")
@Getter
@Setter
@NoArgsConstructor
public class Olt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String ipGerencia;

    private String fabricante;

    private String localizacao;

    private Integer totalPortasPon;

    /* lado inverso do 1:N com Cto (a FK olt_id esta na tabela cto). */
    @OneToMany(mappedBy = "olt")
    private List<Cto> ctos = new ArrayList<>();
}
