package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.enums.TipoOrdemServico;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/** Dados de entrada da OS. O número é gerado pelo sistema e o status começa AGENDADA. */
public record OrdemServicoRequest(
        @NotNull(message = "O contrato é obrigatório")
        Long contratoId,

        /* opcional: OS aberta a partir de um chamado */
        Long chamadoId,

        @NotNull(message = "O técnico é obrigatório")
        Long tecnicoId,

        @NotNull(message = "O tipo é obrigatório (INSTALACAO, REPARO, RETIRADA ou MUDANCA_ENDERECO)")
        TipoOrdemServico tipo,

        @NotNull(message = "A data agendada é obrigatória")
        LocalDateTime dataAgendada,

        @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres")
        String observacoes,

        /* opcional: ids dos equipamentos utilizados (relacionamento N:N) */
        Set<Long> equipamentoIds) {
}
