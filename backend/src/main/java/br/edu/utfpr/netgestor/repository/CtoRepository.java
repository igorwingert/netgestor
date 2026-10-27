package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Cto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CtoRepository extends JpaRepository<Cto, Long> {

    Optional<Cto> findByIdentificacaoIgnoreCase(String identificacao);

    boolean existsByOltId(Long oltId);

    /** Existe CTO ligada numa porta PON acima do total informado? (usado ao reduzir a OLT) */
    boolean existsByOltIdAndPortaPonGreaterThan(Long oltId, Integer portaPon);
}
