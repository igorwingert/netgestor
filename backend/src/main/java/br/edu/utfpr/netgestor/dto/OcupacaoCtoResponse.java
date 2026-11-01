package br.edu.utfpr.netgestor.dto;

import java.util.List;

/**
 * resposta de GET /api/ctos/{id}/ocupacao: situacao de cada porta da CTO.
 * contratos cancelados nao ocupam porta.
 */
public record OcupacaoCtoResponse(
        Long ctoId,
        String identificacao,
        int capacidade,
        int portasOcupadas,
        int portasLivres,
        List<Porta> portas) {

    /** uma porta da CTO: livre, ou ocupada por um contrato. */
    public record Porta(
            int numero,
            boolean livre,
            Long contratoId,
            String contratoNumero,
            String clienteNome) {
    }
}
