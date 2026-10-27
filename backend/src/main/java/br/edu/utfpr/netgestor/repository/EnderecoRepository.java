package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
}
