package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
