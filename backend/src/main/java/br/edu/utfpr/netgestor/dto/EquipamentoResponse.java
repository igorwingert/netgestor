package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Equipamento;
import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "equipamentos", itemRelation = "equipamento")
public record EquipamentoResponse(
        Long id,
        TipoEquipamento tipo,
        String fabricante,
        String modelo,
        String numeroSerie,
        String mac,
        StatusEquipamento status,
        Long contratoId,
        String contratoNumero) {

    public static EquipamentoResponse de(Equipamento equipamento) {
        boolean temContrato = equipamento.getContrato() != null;
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getTipo(),
                equipamento.getFabricante(),
                equipamento.getModelo(),
                equipamento.getNumeroSerie(),
                equipamento.getMac(),
                equipamento.getStatus(),
                temContrato ? equipamento.getContrato().getId() : null,
                temContrato ? equipamento.getContrato().getNumero() : null);
    }
}
