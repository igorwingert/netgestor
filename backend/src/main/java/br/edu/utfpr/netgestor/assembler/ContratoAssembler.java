package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ClienteController;
import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.CtoController;
import br.edu.utfpr.netgestor.controller.EnderecoController;
import br.edu.utfpr.netgestor.controller.PlanoController;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Transforma um ContratoResponse em um EntityModel com links HATEOAS (formato HAL).
 *
 * Exemplo de resposta de um contrato ATIVO:
 * <pre>
 * {
 *   "id": 1, "numero": "CT-2025-0001", "status": "ATIVO", ...,
 *   "_links": {
 *     "self":      { "href": "http://localhost:8080/api/contratos/1" },
 *     "cliente":   { "href": "http://localhost:8080/api/clientes/1" },
 *     "faturas":   { "href": "http://localhost:8080/api/contratos/1/faturas" },
 *     "suspender": { "href": "http://localhost:8080/api/contratos/1/suspender" },
 *     "cancelar":  { "href": "http://localhost:8080/api/contratos/1/cancelar" }
 *   }
 * }
 * </pre>
 *
 * linkTo(methodOn(...)) monta a URL a partir do método do controller: se a rota
 * mudar no controller, o link muda junto (nada de URL escrita "na mão").
 */
@Component
public class ContratoAssembler implements RepresentationModelAssembler<ContratoResponse, EntityModel<ContratoResponse>> {

    @Override
    public EntityModel<ContratoResponse> toModel(ContratoResponse contrato) {
        Long id = contrato.id();

        // Links de navegação: o próprio recurso, a coleção e os recursos relacionados
        EntityModel<ContratoResponse> model = EntityModel.of(contrato,
                linkTo(methodOn(ContratoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(ContratoController.class).withRel("contratos"),
                linkTo(methodOn(ClienteController.class).buscarPorId(contrato.clienteId())).withRel("cliente"),
                linkTo(methodOn(PlanoController.class).buscarPorId(contrato.planoId())).withRel("plano"),
                linkTo(methodOn(EnderecoController.class).buscarPorId(contrato.enderecoInstalacaoId()))
                        .withRel("enderecoInstalacao"),
                linkTo(methodOn(ContratoController.class).listarFaturas(id)).withRel("faturas"),
                linkTo(methodOn(ContratoController.class).listarChamados(id)).withRel("chamados"),
                linkTo(methodOn(ContratoController.class).listarOrdensServico(id)).withRel("ordensServico"),
                linkTo(methodOn(ContratoController.class).listarEquipamentos(id)).withRel("equipamentos"));

        if (contrato.ctoId() != null) {
            model.add(linkTo(methodOn(CtoController.class).buscarPorId(contrato.ctoId())).withRel("cto"));
        }

        // Links condicionais (evidência do nível 3 de Richardson): cada ação só aparece
        // quando é permitida no status atual. O frontend mostra os botões a partir deles.
        if (contrato.status().podeSuspender()) {
            model.add(linkTo(methodOn(ContratoController.class).suspender(id)).withRel("suspender"));
        }
        if (contrato.status().podeReativar()) {
            model.add(linkTo(methodOn(ContratoController.class).reativar(id)).withRel("reativar"));
        }
        if (contrato.status().podeCancelar()) {
            model.add(linkTo(methodOn(ContratoController.class).cancelar(id)).withRel("cancelar"));
        }
        return model;
    }

    /** Coleção: cada item recebe os links de toModel, e a coleção ganha o próprio "self". */
    @Override
    public CollectionModel<EntityModel<ContratoResponse>> toCollectionModel(
            Iterable<? extends ContratoResponse> contratos) {
        return RepresentationModelAssembler.super.toCollectionModel(contratos)
                .add(LinkUtil.selfDaRequisicao());
    }
}
