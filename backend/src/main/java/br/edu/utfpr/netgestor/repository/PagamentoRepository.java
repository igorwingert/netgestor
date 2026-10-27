package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByFaturaIdOrderByDataPagamento(Long faturaId);
}
