package br.edu.utfpr.netgestor.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato único de erro da API. Exemplo:
 * <pre>
 * {
 *   "timestamp": "2026-10-26T22:00:00",
 *   "status": 409,
 *   "erro": "Conflict",
 *   "mensagem": "Já existe um cliente com o CPF/CNPJ 12345678909",
 *   "caminho": "/api/clientes",
 *   "campos": []
 * }
 * </pre>
 */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<Campo> campos) {

    /** Erro de validação de um campo específico (usado no 400). */
    public record Campo(String campo, String mensagem) {
    }
}
