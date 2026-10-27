package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
}
