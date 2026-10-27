package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    Optional<Equipamento> findByNumeroSerie(String numeroSerie);

    Optional<Equipamento> findByMac(String mac);

    boolean existsByContratoId(Long contratoId);
}
