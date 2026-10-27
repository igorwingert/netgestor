package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Fatura;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    boolean existsByContratoId(Long contratoId);

    Optional<Fatura> findByContratoIdAndCompetencia(Long contratoId, LocalDate competencia);

    /**
     * GET /api/faturas?status=VENCIDA&competencia=2026-10  e  GET /api/contratos/{id}/faturas
     * O CAST é necessário porque o driver do PostgreSQL envia datas sem tipo definido,
     * e o banco não consegue resolver "? IS NULL" sozinho ("could not determine data type").
     */
    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    @Query("""
            SELECT f FROM Fatura f
            WHERE (:status IS NULL OR f.status = :status)
              AND (CAST(:competencia AS LocalDate) IS NULL OR f.competencia = :competencia)
              AND (:contratoId IS NULL OR f.contrato.id = :contratoId)
            ORDER BY f.competencia, f.id
            """)
    List<Fatura> buscar(@Param("status") StatusFatura status,
                        @Param("competencia") LocalDate competencia,
                        @Param("contratoId") Long contratoId);
}
