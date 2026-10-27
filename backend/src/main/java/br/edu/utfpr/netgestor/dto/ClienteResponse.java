package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Cliente;
import br.edu.utfpr.netgestor.domain.enums.TipoPessoa;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@Relation(collectionRelation = "clientes", itemRelation = "cliente")
public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        TipoPessoa tipoPessoa,
        String email,
        String telefone,
        LocalDate dataCadastro,
        boolean ativo) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTipoPessoa(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getDataCadastro(),
                cliente.isAtivo());
    }
}
