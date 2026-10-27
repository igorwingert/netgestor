package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ClienteController;
import br.edu.utfpr.netgestor.controller.EnderecoController;
import br.edu.utfpr.netgestor.dto.EnderecoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EnderecoAssembler implements RepresentationModelAssembler<EnderecoResponse, EntityModel<EnderecoResponse>> {

    @Override
    public EntityModel<EnderecoResponse> toModel(EnderecoResponse endereco) {
        return EntityModel.of(endereco,
                linkTo(methodOn(EnderecoController.class).buscarPorId(endereco.id())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).buscarPorId(endereco.clienteId())).withRel("cliente"));
    }

    @Override
    public CollectionModel<EntityModel<EnderecoResponse>> toCollectionModel(
            Iterable<? extends EnderecoResponse> enderecos) {
        return RepresentationModelAssembler.super.toCollectionModel(enderecos).add(LinkUtil.selfDaRequisicao());
    }
}
