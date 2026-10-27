package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByClienteIdOrderById(Long clienteId);
}
