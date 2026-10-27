package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByPlanoId(Long planoId);

    boolean existsByCtoId(Long ctoId);

    boolean existsByEnderecoInstalacaoId(Long enderecoId);

    /** Contrato (não cancelado) que ocupa a porta da CTO, se houver. */
    Optional<Contrato> findByCtoIdAndPortaCtoAndStatusNot(Long ctoId, Integer portaCto, StatusContrato status);

    /** Existe contrato (não cancelado) numa porta acima da capacidade informada? (usado ao reduzir a CTO) */
    boolean existsByCtoIdAndPortaCtoGreaterThanAndStatusNot(Long ctoId, Integer portaCto, StatusContrato status);

    /** Portas em uso numa CTO, para a consulta de ocupação. */
    List<Contrato> findByCtoIdAndStatusNotOrderByPortaCto(Long ctoId, StatusContrato status);

    /** Usado para gerar o próximo número (ex.: CT-2026-0003). */
    long countByNumeroStartingWith(String prefixo);

    /**
     * GET /api/contratos?status=&clienteId=  (filtro nulo = não filtra por aquele campo)
     * O @EntityGraph carrega cliente, plano, endereço e CTO no mesmo SELECT (JOIN),
     * evitando o problema "N+1" (uma consulta extra por contrato ao montar a resposta).
     */
    @EntityGraph(attributePaths = {"cliente", "plano", "enderecoInstalacao", "cto"})
    @Query("""
            SELECT c FROM Contrato c
            WHERE (:status IS NULL OR c.status = :status)
              AND (:clienteId IS NULL OR c.cliente.id = :clienteId)
            ORDER BY c.numero
            """)
    List<Contrato> buscar(@Param("status") StatusContrato status, @Param("clienteId") Long clienteId);
}
