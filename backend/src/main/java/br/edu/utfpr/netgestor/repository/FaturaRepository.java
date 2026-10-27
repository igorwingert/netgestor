package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    boolean existsByContratoId(Long contratoId);

    Optional<Fatura> findByContratoIdAndCompetencia(Long contratoId, LocalDate competencia);
}
