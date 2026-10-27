package br.edu.utfpr.netgestor.domain;

import br.edu.utfpr.netgestor.domain.enums.TipoPessoa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Cliente do provedor (pessoa física ou jurídica). */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    /** CPF (11 dígitos) ou CNPJ (14 dígitos), somente números. */
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    private String email;

    private String telefone;

    private LocalDate dataCadastro;

    private boolean ativo = true;

    /*
     * Lado "inverso" do relacionamento 1:N: quem tem a coluna cliente_id é o Endereco
     * (por isso o mappedBy). Os endereços pertencem ao cliente: cascade ALL salva e
     * exclui junto, e orphanRemoval apaga o endereço que for removido da lista.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    /** Mantém os dois lados do relacionamento sincronizados. */
    public void adicionarEndereco(Endereco endereco) {
        endereco.setCliente(this);
        enderecos.add(endereco);
    }

    /** Executado pelo JPA antes do INSERT: a data de cadastro é sempre a do dia. */
    @PrePersist
    void aoCadastrar() {
        if (dataCadastro == null) {
            dataCadastro = LocalDate.now();
        }
    }
}
