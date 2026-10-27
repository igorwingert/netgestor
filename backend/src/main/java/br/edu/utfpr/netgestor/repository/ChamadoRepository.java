package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Chamado;
import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    boolean existsByContratoId(Long contratoId);

    boolean existsByAbertoPorId(Long usuarioId);

    /** Usado para gerar o próximo protocolo (ex.: CH-2026-000006). */
    long countByProtocoloStartingWith(String prefixo);

    /** GET /api/chamados?status=&prioridade=  e  GET /api/contratos/{id}/chamados (mais recentes primeiro) */
    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "abertoPor"})
    @Query("""
            SELECT ch FROM Chamado ch
            WHERE (:status IS NULL OR ch.status = :status)
              AND (:prioridade IS NULL OR ch.prioridade = :prioridade)
              AND (:contratoId IS NULL OR ch.contrato.id = :contratoId)
            ORDER BY ch.dataAbertura DESC
            """)
    List<Chamado> buscar(@Param("status") StatusChamado status,
                         @Param("prioridade") Prioridade prioridade,
                         @Param("contratoId") Long contratoId);
}
