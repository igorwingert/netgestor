package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ChamadoController;
import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.OrdemServicoController;
import br.edu.utfpr.netgestor.controller.TecnicoController;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import br.edu.utfpr.netgestor.dto.OrdemServicoResponse;
import br.edu.utfpr.netgestor.security.Permissoes;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrdemServicoAssembler
        implements RepresentationModelAssembler<OrdemServicoResponse, EntityModel<OrdemServicoResponse>> {

    @Override
    public EntityModel<OrdemServicoResponse> toModel(OrdemServicoResponse os) {
        Long id = os.id();
        EntityModel<OrdemServicoResponse> model = EntityModel.of(os,
                linkTo(methodOn(OrdemServicoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(OrdemServicoController.class).withRel("ordensServico"),
                linkTo(methodOn(ContratoController.class).buscarPorId(os.contratoId())).withRel("contrato"),
                linkTo(methodOn(TecnicoController.class).buscarPorId(os.tecnicoId())).withRel("tecnico"));

        if (os.chamadoId() != null) {
            model.add(linkTo(methodOn(ChamadoController.class).buscarPorId(os.chamadoId())).withRel("chamado"));
        }

        // AGENDADA -> iniciar -> EM_EXECUCAO -> concluir; cancelar enquanto não concluída.
        // Cada link exige o status certo E um perfil que possa executar a ação.
        // (methodOn recebe null no usuário: o parâmetro só existe na execução, não na URL)
        boolean podeExecutar = Permissoes.usuarioTemPerfil(Perfil.ADMIN, Perfil.TECNICO);
        boolean podeGerenciar = Permissoes.usuarioTemPerfil(Perfil.ADMIN, Perfil.ATENDENTE);
        if (podeExecutar && os.status().podeIniciar()) {
            model.add(linkTo(methodOn(OrdemServicoController.class).iniciar(id, null)).withRel("iniciar"));
        }
        if (podeExecutar && os.status().podeConcluir()) {
            model.add(linkTo(methodOn(OrdemServicoController.class).concluir(id, null)).withRel("concluir"));
        }
        if (podeGerenciar && os.status().podeCancelar()) {
            model.add(linkTo(methodOn(OrdemServicoController.class).cancelar(id)).withRel("cancelar"));
        }
        return model;
    }

    @Override
    public CollectionModel<EntityModel<OrdemServicoResponse>> toCollectionModel(
            Iterable<? extends OrdemServicoResponse> ordens) {
        return RepresentationModelAssembler.super.toCollectionModel(ordens).add(LinkUtil.selfDaRequisicao());
    }
}
