package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.TipoChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada do chamado. O protocolo é gerado pelo sistema, o status começa ABERTO
 * e quem abre o chamado é o usuário logado (dono do token JWT), não um campo do corpo.
 */
public record ChamadoRequest(
        @NotNull(message = "O contrato é obrigatório")
        Long contratoId,

        @NotNull(message = "O tipo é obrigatório (SEM_CONEXAO, LENTIDAO, FINANCEIRO ou OUTRO)")
        TipoChamado tipo,

        @NotNull(message = "A prioridade é obrigatória (BAIXA, MEDIA, ALTA ou CRITICA)")
        Prioridade prioridade,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao) {
}
