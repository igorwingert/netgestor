package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Equipamento;
import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    Optional<Equipamento> findByNumeroSerie(String numeroSerie);

    Optional<Equipamento> findByMac(String mac);

    boolean existsByContratoId(Long contratoId);

    /** GET /api/equipamentos?status=&tipo=  e  GET /api/contratos/{id}/equipamentos */
    @Query("""
            SELECT e FROM Equipamento e
            WHERE (:status IS NULL OR e.status = :status)
              AND (:tipo IS NULL OR e.tipo = :tipo)
              AND (:contratoId IS NULL OR e.contrato.id = :contratoId)
            ORDER BY e.id
            """)
    List<Equipamento> buscar(@Param("status") StatusEquipamento status,
                             @Param("tipo") TipoEquipamento tipo,
                             @Param("contratoId") Long contratoId);
}
