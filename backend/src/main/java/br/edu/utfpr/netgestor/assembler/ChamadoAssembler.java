package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ChamadoController;
import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.UsuarioController;
import br.edu.utfpr.netgestor.dto.ChamadoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ChamadoAssembler implements RepresentationModelAssembler<ChamadoResponse, EntityModel<ChamadoResponse>> {

    @Override
    public EntityModel<ChamadoResponse> toModel(ChamadoResponse chamado) {
        Long id = chamado.id();
        EntityModel<ChamadoResponse> model = EntityModel.of(chamado,
                linkTo(methodOn(ChamadoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(ChamadoController.class).withRel("chamados"),
                linkTo(methodOn(ContratoController.class).buscarPorId(chamado.contratoId())).withRel("contrato"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(chamado.abertoPorId())).withRel("abertoPor"));

        // ciclo de vida: ABERTO -> iniciar-atendimento -> resolver -> fechar
        if (chamado.status().podeIniciarAtendimento()) {
            model.add(linkTo(methodOn(ChamadoController.class).iniciarAtendimento(id)).withRel("iniciar-atendimento"));
        }
        if (chamado.status().podeResolver()) {
            model.add(linkTo(methodOn(ChamadoController.class).resolver(id)).withRel("resolver"));
        }
        if (chamado.status().podeFechar()) {
            model.add(linkTo(methodOn(ChamadoController.class).fechar(id)).withRel("fechar"));
        }
        return model;
    }

    @Override
    public CollectionModel<EntityModel<ChamadoResponse>> toCollectionModel(Iterable<? extends ChamadoResponse> chamados) {
        return RepresentationModelAssembler.super.toCollectionModel(chamados).add(LinkUtil.selfDaRequisicao());
    }
}
