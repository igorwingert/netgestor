package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;

import java.time.LocalDate;

/**
 * Além dos ids, traz nomes "achatados" (clienteNome, planoNome...) para o frontend
 * exibir a listagem sem precisar de uma requisição extra por contrato.
 */
public record ContratoResponse(
        Long id,
        String numero,
        Long clienteId,
        String clienteNome,
        Long planoId,
        String planoNome,
        Long enderecoInstalacaoId,
        String enderecoInstalacao,
        Long ctoId,
        String ctoIdentificacao,
        Integer portaCto,
        LocalDate dataInicio,
        Integer diaVencimento,
        StatusContrato status) {

    public static ContratoResponse de(Contrato contrato) {
        boolean temCto = contrato.getCto() != null;
        return new ContratoResponse(
                contrato.getId(),
                contrato.getNumero(),
                contrato.getCliente().getId(),
                contrato.getCliente().getNome(),
                contrato.getPlano().getId(),
                contrato.getPlano().getNome(),
                contrato.getEnderecoInstalacao().getId(),
                contrato.getEnderecoInstalacao().resumo(),
                temCto ? contrato.getCto().getId() : null,
                temCto ? contrato.getCto().getIdentificacao() : null,
                contrato.getPortaCto(),
                contrato.getDataInicio(),
                contrato.getDiaVencimento(),
                contrato.getStatus());
    }
}
