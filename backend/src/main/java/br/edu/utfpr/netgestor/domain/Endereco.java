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

/** endereco de um cliente. pode ser usado como endereco de instalacao de contratos. */
@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** CEP somente com digitos (8). */
    private String cep;

    private String logradouro;

    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String uf;

    /* lado "dono" do relacionamento: e aqui que fica a chave estrangeira cliente_id. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** endereco em uma linha, ex.: "Rua XV de Novembro, 1000 - Centro, Curitiba/PR". */
    public String resumo() {
        return logradouro + ", " + numero + " - " + bairro + ", " + cidade + "/" + uf;
    }
}
