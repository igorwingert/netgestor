package br.edu.utfpr.netgestor.assembler;

import br.edu.utfpr.netgestor.controller.ClienteController;
import br.edu.utfpr.netgestor.controller.ContratoController;
import br.edu.utfpr.netgestor.controller.CtoController;
import br.edu.utfpr.netgestor.controller.EnderecoController;
import br.edu.utfpr.netgestor.controller.PlanoController;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import br.edu.utfpr.netgestor.security.Permissoes;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * transforma um ContratoResponse em um EntityModel com links HATEOAS (formato HAL).
 *
 * exemplo de resposta de um contrato ATIVO:
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
 * linkTo(methodOn(...)) monta a URL a partir do metodo do controller: se a rota
 * mudar no controller, o link muda junto (nada de URL escrita "na mão").
 */
@Component
public class ContratoAssembler implements RepresentationModelAssembler<ContratoResponse, EntityModel<ContratoResponse>> {

    @Override
    public EntityModel<ContratoResponse> toModel(ContratoResponse contrato) {
        Long id = contrato.id();

        // links de navegacao visiveis para todos os perfis que leem contratos
        EntityModel<ContratoResponse> model = EntityModel.of(contrato,
                linkTo(methodOn(ContratoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(ContratoController.class).withRel("contratos"),
                linkTo(methodOn(ContratoController.class).listarOrdensServico(id)).withRel("ordensServico"),
                linkTo(methodOn(ContratoController.class).listarEquipamentos(id)).withRel("equipamentos"));

        if (contrato.ctoId() != null) {
            model.add(linkTo(methodOn(CtoController.class).buscarPorId(contrato.ctoId())).withRel("cto"));
        }

        // Cliente, plano, endereco, faturas e chamados: so para quem pode acessa-los (ADMIN/ATENDENTE).
        // assim o TECNICO nao recebe links que dariam 403.
        boolean podeGerenciar = Permissoes.usuarioTemPerfil(Perfil.ADMIN, Perfil.ATENDENTE);
        if (podeGerenciar) {
            model.add(linkTo(methodOn(ClienteController.class).buscarPorId(contrato.clienteId())).withRel("cliente"));
            model.add(linkTo(methodOn(PlanoController.class).buscarPorId(contrato.planoId())).withRel("plano"));
            model.add(linkTo(methodOn(EnderecoController.class).buscarPorId(contrato.enderecoInstalacaoId()))
                    .withRel("enderecoInstalacao"));
            model.add(linkTo(methodOn(ContratoController.class).listarFaturas(id)).withRel("faturas"));
            model.add(linkTo(methodOn(ContratoController.class).listarChamados(id)).withRel("chamados"));
        }

        // links condicionais (evidencia do nivel 3 de Richardson): cada acao so aparece
        // quando e permitida no status atual e para um perfil que pode executa-la
        // (o mesmo criterio do @PreAuthorize do controller). o frontend mostra os botoes a partir deles.
        if (podeGerenciar && contrato.status().podeSuspender()) {
            model.add(linkTo(methodOn(ContratoController.class).suspender(id)).withRel("suspender"));
        }
        if (podeGerenciar && contrato.status().podeReativar()) {
            model.add(linkTo(methodOn(ContratoController.class).reativar(id)).withRel("reativar"));
        }
        if (podeGerenciar && contrato.status().podeCancelar()) {
            model.add(linkTo(methodOn(ContratoController.class).cancelar(id)).withRel("cancelar"));
        }
        return model;
    }

    /** colecao: cada item recebe os links de toModel, e a colecao ganha o proprio "self". */
    @Override
    public CollectionModel<EntityModel<ContratoResponse>> toCollectionModel(
            Iterable<? extends ContratoResponse> contratos) {
        return RepresentationModelAssembler.super.toCollectionModel(contratos)
                .add(LinkUtil.selfDaRequisicao());
    }
}
