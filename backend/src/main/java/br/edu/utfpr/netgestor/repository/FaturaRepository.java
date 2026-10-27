package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {
}
