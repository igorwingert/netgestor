package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.CtoController;
import br.edu.utfpr.netgestor.controller.OltController;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CtoAssembler implements RepresentationModelAssembler<CtoResponse, EntityModel<CtoResponse>> {

    @Override
    public EntityModel<CtoResponse> toModel(CtoResponse cto) {
        return EntityModel.of(cto,
                linkTo(methodOn(CtoController.class).buscarPorId(cto.id())).withSelfRel(),
                linkTo(CtoController.class).withRel("ctos"),
                linkTo(methodOn(OltController.class).buscarPorId(cto.oltId())).withRel("olt"),
                linkTo(methodOn(CtoController.class).ocupacao(cto.id())).withRel("ocupacao"));
    }

    @Override
    public CollectionModel<EntityModel<CtoResponse>> toCollectionModel(Iterable<? extends CtoResponse> ctos) {
        return RepresentationModelAssembler.super.toCollectionModel(ctos).add(LinkUtil.selfDaRequisicao());
    }
}
