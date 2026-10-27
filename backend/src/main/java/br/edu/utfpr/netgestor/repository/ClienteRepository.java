package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
