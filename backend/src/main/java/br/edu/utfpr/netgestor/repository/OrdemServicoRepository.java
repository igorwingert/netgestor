package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
}
