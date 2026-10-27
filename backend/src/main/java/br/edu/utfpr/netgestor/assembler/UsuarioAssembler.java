package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.UsuarioController;
import br.edu.utfpr.netgestor.dto.UsuarioResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioAssembler implements RepresentationModelAssembler<UsuarioResponse, EntityModel<UsuarioResponse>> {

    @Override
    public EntityModel<UsuarioResponse> toModel(UsuarioResponse usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.id())).withSelfRel(),
                linkTo(UsuarioController.class).withRel("usuarios"));
    }

    @Override
    public CollectionModel<EntityModel<UsuarioResponse>> toCollectionModel(Iterable<? extends UsuarioResponse> usuarios) {
        return RepresentationModelAssembler.super.toCollectionModel(usuarios).add(LinkUtil.selfDaRequisicao());
    }
}
