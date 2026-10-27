package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.PlanoController;
import br.edu.utfpr.netgestor.dto.PlanoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PlanoAssembler implements RepresentationModelAssembler<PlanoResponse, EntityModel<PlanoResponse>> {

    @Override
    public EntityModel<PlanoResponse> toModel(PlanoResponse plano) {
        return EntityModel.of(plano,
                linkTo(methodOn(PlanoController.class).buscarPorId(plano.id())).withSelfRel(),
                linkTo(PlanoController.class).withRel("planos"));
    }

    @Override
    public CollectionModel<EntityModel<PlanoResponse>> toCollectionModel(Iterable<? extends PlanoResponse> planos) {
        return RepresentationModelAssembler.super.toCollectionModel(planos).add(LinkUtil.selfDaRequisicao());
    }
}
