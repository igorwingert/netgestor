package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.OrdemServico;
import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import br.edu.utfpr.netgestor.domain.enums.TipoOrdemServico;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Relation(collectionRelation = "ordensServico", itemRelation = "ordemServico")
public record OrdemServicoResponse(
        Long id,
        String numero,
        Long contratoId,
        String contratoNumero,
        String enderecoInstalacao,
        Long chamadoId,
        String chamadoProtocolo,
        Long tecnicoId,
        String tecnicoNome,
        TipoOrdemServico tipo,
        StatusOrdemServico status,
        LocalDateTime dataAgendada,
        LocalDateTime dataConclusao,
        String observacoes,
        List<EquipamentoResponse> equipamentos) {

    public static OrdemServicoResponse de(OrdemServico os) {
        boolean temChamado = os.getChamado() != null;
        List<EquipamentoResponse> equipamentos = os.getEquipamentos().stream()
                .sorted(Comparator.comparing(e -> e.getId()))
                .map(EquipamentoResponse::de)
                .toList();
        return new OrdemServicoResponse(
                os.getId(),
                os.getNumero(),
                os.getContrato().getId(),
                os.getContrato().getNumero(),
                os.getContrato().getEnderecoInstalacao().resumo(),
                temChamado ? os.getChamado().getId() : null,
                temChamado ? os.getChamado().getProtocolo() : null,
                os.getTecnico().getId(),
                os.getTecnico().getNome(),
                os.getTipo(),
                os.getStatus(),
                os.getDataAgendada(),
                os.getDataConclusao(),
                os.getObservacoes(),
                equipamentos);
    }
}
