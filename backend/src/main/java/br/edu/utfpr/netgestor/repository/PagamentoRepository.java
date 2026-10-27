package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
