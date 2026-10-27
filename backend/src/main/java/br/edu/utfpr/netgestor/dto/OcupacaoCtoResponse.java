package br.edu.utfpr.netgestor.dto;

import java.util.List;

/**
 * Resposta de GET /api/ctos/{id}/ocupacao: situação de cada porta da CTO.
 * Contratos cancelados não ocupam porta.
 */
public record OcupacaoCtoResponse(
        Long ctoId,
        String identificacao,
        int capacidade,
        int portasOcupadas,
        int portasLivres,
        List<Porta> portas) {

    /** Uma porta da CTO: livre, ou ocupada por um contrato. */
    public record Porta(
            int numero,
            boolean livre,
            Long contratoId,
            String contratoNumero,
            String clienteNome) {
    }
}
