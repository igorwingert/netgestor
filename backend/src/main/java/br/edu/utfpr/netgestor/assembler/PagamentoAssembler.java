package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.FaturaController;
import br.edu.utfpr.netgestor.controller.PagamentoController;
import br.edu.utfpr.netgestor.dto.PagamentoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PagamentoAssembler
        implements RepresentationModelAssembler<PagamentoResponse, EntityModel<PagamentoResponse>> {

    @Override
    public EntityModel<PagamentoResponse> toModel(PagamentoResponse pagamento) {
        return EntityModel.of(pagamento,
                linkTo(methodOn(PagamentoController.class).buscarPorId(pagamento.id())).withSelfRel(),
                linkTo(methodOn(FaturaController.class).buscarPorId(pagamento.faturaId())).withRel("fatura"));
    }

    @Override
    public CollectionModel<EntityModel<PagamentoResponse>> toCollectionModel(
            Iterable<? extends PagamentoResponse> pagamentos) {
        return RepresentationModelAssembler.super.toCollectionModel(pagamentos).add(LinkUtil.selfDaRequisicao());
    }
}
