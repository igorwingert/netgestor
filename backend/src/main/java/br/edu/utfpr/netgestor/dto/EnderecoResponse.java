package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Endereco;

public record EnderecoResponse(
        Long id,
        Long clienteId,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {

    public static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getCliente().getId(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf());
    }
}
