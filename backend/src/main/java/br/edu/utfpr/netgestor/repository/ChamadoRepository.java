package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
}
