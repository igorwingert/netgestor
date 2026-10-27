package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
}
