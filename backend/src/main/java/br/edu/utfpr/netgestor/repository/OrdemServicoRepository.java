package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    boolean existsByContratoId(Long contratoId);

    boolean existsByChamadoId(Long chamadoId);

    boolean existsByTecnicoId(Long tecnicoId);

    /** Navega no relacionamento N:N: existe OS que usou este equipamento? */
    boolean existsByEquipamentosId(Long equipamentoId);

    /** Usado para gerar o próximo número (ex.: OS-2026-0006). */
    long countByNumeroStartingWith(String prefixo);
}
