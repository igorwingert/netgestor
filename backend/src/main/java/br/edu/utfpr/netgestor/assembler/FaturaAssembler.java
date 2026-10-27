package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.FaturaController;
import br.edu.utfpr.netgestor.controller.PagamentoController;
import br.edu.utfpr.netgestor.dto.FaturaResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FaturaAssembler implements RepresentationModelAssembler<FaturaResponse, EntityModel<FaturaResponse>> {

    @Override
    public EntityModel<FaturaResponse> toModel(FaturaResponse fatura) {
        Long id = fatura.id();
        EntityModel<FaturaResponse> model = EntityModel.of(fatura,
                linkTo(methodOn(FaturaController.class).buscarPorId(id)).withSelfRel(),
                linkTo(FaturaController.class).withRel("faturas"),
                linkTo(methodOn(ContratoController.class).buscarPorId(fatura.contratoId())).withRel("contrato"),
                linkTo(methodOn(PagamentoController.class).listarPorFatura(id)).withRel("pagamentos"));

        // Fatura em aberto (ABERTA ou VENCIDA) pode receber pagamento ou ser cancelada
        if (fatura.status().emAberto()) {
            model.add(linkTo(methodOn(PagamentoController.class).cadastrar(id, null)).withRel("registrar-pagamento"));
            model.add(linkTo(methodOn(FaturaController.class).cancelar(id)).withRel("cancelar"));
        }
        return model;
    }

    @Override
    public CollectionModel<EntityModel<FaturaResponse>> toCollectionModel(Iterable<? extends FaturaResponse> faturas) {
        return RepresentationModelAssembler.super.toCollectionModel(faturas).add(LinkUtil.selfDaRequisicao());
    }
}
