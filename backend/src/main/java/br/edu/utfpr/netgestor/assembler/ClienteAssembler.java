package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ClienteController;
import br.edu.utfpr.netgestor.controller.EnderecoController;
import br.edu.utfpr.netgestor.dto.ClienteResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClienteAssembler implements RepresentationModelAssembler<ClienteResponse, EntityModel<ClienteResponse>> {

    @Override
    public EntityModel<ClienteResponse> toModel(ClienteResponse cliente) {
        Long id = cliente.id();
        return EntityModel.of(cliente,
                linkTo(methodOn(ClienteController.class).buscarPorId(id)).withSelfRel(),
                linkTo(ClienteController.class).withRel("clientes"),
                linkTo(methodOn(EnderecoController.class).listarPorCliente(id)).withRel("enderecos"),
                linkTo(methodOn(ClienteController.class).listarContratos(id)).withRel("contratos"));
    }

    @Override
    public CollectionModel<EntityModel<ClienteResponse>> toCollectionModel(Iterable<? extends ClienteResponse> clientes) {
        return RepresentationModelAssembler.super.toCollectionModel(clientes).add(LinkUtil.selfDaRequisicao());
    }
}
