package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.OltController;
import br.edu.utfpr.netgestor.dto.OltResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OltAssembler implements RepresentationModelAssembler<OltResponse, EntityModel<OltResponse>> {

    @Override
    public EntityModel<OltResponse> toModel(OltResponse olt) {
        return EntityModel.of(olt,
                linkTo(methodOn(OltController.class).buscarPorId(olt.id())).withSelfRel(),
                linkTo(OltController.class).withRel("olts"),
                linkTo(methodOn(OltController.class).listarCtos(olt.id())).withRel("ctos"));
    }

    @Override
    public CollectionModel<EntityModel<OltResponse>> toCollectionModel(Iterable<? extends OltResponse> olts) {
        return RepresentationModelAssembler.super.toCollectionModel(olts).add(LinkUtil.selfDaRequisicao());
    }
}
