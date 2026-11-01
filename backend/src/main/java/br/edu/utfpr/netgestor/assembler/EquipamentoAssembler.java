package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.EquipamentoController;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.security.Permissoes;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EquipamentoAssembler
        implements RepresentationModelAssembler<EquipamentoResponse, EntityModel<EquipamentoResponse>> {

    @Override
    public EntityModel<EquipamentoResponse> toModel(EquipamentoResponse equipamento) {
        Long id = equipamento.id();
        EntityModel<EquipamentoResponse> model = EntityModel.of(equipamento,
                linkTo(methodOn(EquipamentoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(EquipamentoController.class).withRel("equipamentos"));

        if (equipamento.contratoId() != null) {
            model.add(linkTo(methodOn(ContratoController.class).buscarPorId(equipamento.contratoId()))
                    .withRel("contrato"));
        }

        // comodato: em ESTOQUE pode ser vinculado; INSTALADO pode ser desvinculado
        boolean podeOperar = Permissoes.usuarioTemPerfil(Perfil.ADMIN, Perfil.TECNICO);
        if (podeOperar && equipamento.status().podeVincular()) {
            model.add(linkTo(methodOn(EquipamentoController.class).vincular(id, null)).withRel("vincular"));
        }
        if (podeOperar && equipamento.status().podeDesvincular()) {
            model.add(linkTo(methodOn(EquipamentoController.class).desvincular(id)).withRel("desvincular"));
        }
        return model;
    }

    @Override
    public CollectionModel<EntityModel<EquipamentoResponse>> toCollectionModel(
            Iterable<? extends EquipamentoResponse> equipamentos) {
        return RepresentationModelAssembler.super.toCollectionModel(equipamentos).add(LinkUtil.selfDaRequisicao());
    }
}
