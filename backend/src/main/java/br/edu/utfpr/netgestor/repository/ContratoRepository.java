package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import org.springframework.data.jpa.repository.JpaRepository;

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

    /** Usado para gerar o próximo número (ex.: CT-2026-0003). */
    long countByNumeroStartingWith(String prefixo);
}
