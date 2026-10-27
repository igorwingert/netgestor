package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    boolean existsByContratoId(Long contratoId);

    boolean existsByAbertoPorId(Long usuarioId);

    /** Usado para gerar o próximo protocolo (ex.: CH-2026-000006). */
    long countByProtocoloStartingWith(String prefixo);
}
