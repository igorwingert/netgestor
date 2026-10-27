package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

    Optional<Plano> findByNomeIgnoreCase(String nome);
}
