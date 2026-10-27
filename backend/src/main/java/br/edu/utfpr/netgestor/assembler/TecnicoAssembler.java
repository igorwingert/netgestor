package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.TecnicoController;
import br.edu.utfpr.netgestor.controller.UsuarioController;
import br.edu.utfpr.netgestor.dto.TecnicoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TecnicoAssembler implements RepresentationModelAssembler<TecnicoResponse, EntityModel<TecnicoResponse>> {

    @Override
    public EntityModel<TecnicoResponse> toModel(TecnicoResponse tecnico) {
        Long id = tecnico.id();
        return EntityModel.of(tecnico,
                linkTo(methodOn(TecnicoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(TecnicoController.class).withRel("tecnicos"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(tecnico.usuarioId())).withRel("usuario"),
                // o parâmetro opcional ?status vira um link "templated": .../ordens-servico{?status}
                linkTo(methodOn(TecnicoController.class).listarOrdensServico(id, null)).withRel("ordensServico"));
    }

    @Override
    public CollectionModel<EntityModel<TecnicoResponse>> toCollectionModel(Iterable<? extends TecnicoResponse> tecnicos) {
        return RepresentationModelAssembler.super.toCollectionModel(tecnicos).add(LinkUtil.selfDaRequisicao());
    }
}
