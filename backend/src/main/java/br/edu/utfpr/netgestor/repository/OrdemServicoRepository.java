package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.OrdemServico;
import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    boolean existsByContratoId(Long contratoId);

    boolean existsByChamadoId(Long chamadoId);

    boolean existsByTecnicoId(Long tecnicoId);

    /** navega no relacionamento N:N: existe OS que usou este equipamento? */
    boolean existsByEquipamentosId(Long equipamentoId);

    /** usado para gerar o proximo numero (ex.: OS-2026-0006). */
    long countByNumeroStartingWith(String prefixo);

    /**
     * GET /api/ordens-servico?status=&tecnicoId=,  GET /api/tecnicos/{id}/ordens-servico?status=
     * e GET /api/contratos/{id}/ordens-servico (ordenado pela agenda).
     */
    @EntityGraph(attributePaths = {"contrato", "contrato.enderecoInstalacao", "chamado", "tecnico"})
    @Query("""
            SELECT os FROM OrdemServico os
            WHERE (:status IS NULL OR os.status = :status)
              AND (:tecnicoId IS NULL OR os.tecnico.id = :tecnicoId)
              AND (:contratoId IS NULL OR os.contrato.id = :contratoId)
            ORDER BY os.dataAgendada
            """)
    List<OrdemServico> buscar(@Param("status") StatusOrdemServico status,
                              @Param("tecnicoId") Long tecnicoId,
                              @Param("contratoId") Long contratoId);
}
