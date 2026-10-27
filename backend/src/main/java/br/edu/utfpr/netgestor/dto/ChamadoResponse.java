package br.edu.utfpr.netgestor.dto;

import br.edu.utfpr.netgestor.domain.Chamado;
import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import br.edu.utfpr.netgestor.domain.enums.TipoChamado;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "chamados", itemRelation = "chamado")
public record ChamadoResponse(
        Long id,
        String protocolo,
        Long contratoId,
        String contratoNumero,
        String clienteNome,
        Long abertoPorId,
        String abertoPorNome,
        TipoChamado tipo,
        Prioridade prioridade,
        StatusChamado status,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento) {

    public static ChamadoResponse de(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getContrato().getId(),
                chamado.getContrato().getNumero(),
                chamado.getContrato().getCliente().getNome(),
                chamado.getAbertoPor().getId(),
                chamado.getAbertoPor().getNome(),
                chamado.getTipo(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getDescricao(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento());
    }
}
