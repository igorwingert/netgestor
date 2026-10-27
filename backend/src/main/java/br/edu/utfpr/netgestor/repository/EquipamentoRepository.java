package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
}
