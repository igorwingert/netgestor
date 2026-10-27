package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    Optional<Tecnico> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
