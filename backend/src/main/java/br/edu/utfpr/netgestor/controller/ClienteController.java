package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.ClienteAssembler;
import br.edu.utfpr.netgestor.assembler.ContratoAssembler;
import br.edu.utfpr.netgestor.dto.ClienteRequest;
import br.edu.utfpr.netgestor.dto.ClienteResponse;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import br.edu.utfpr.netgestor.service.ClienteService;
import br.edu.utfpr.netgestor.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recurso /api/clientes.
 *
 * O controller só cuida do HTTP (rota, verbo, status, validação de entrada);
 * as regras de negócio ficam no service e os links HATEOAS no assembler.
 *   GET    /api/clientes?nome=&cpfCnpj=   200 lista (filtros opcionais)
 *   GET    /api/clientes/{id}             200 ou 404
 *   POST   /api/clientes                  201 Created + header Location (ou 400/409)
 *   PUT    /api/clientes/{id}             200 (ou 400/404/409)
 *   DELETE /api/clientes/{id}             204 No Content (ou 404/409)
 *   GET    /api/clientes/{id}/contratos   sub-recurso (os endereços ficam no EnderecoController)
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteAssembler clienteAssembler;
    private final ContratoService contratoService;
    private final ContratoAssembler contratoAssembler;

    @GetMapping
    public CollectionModel<EntityModel<ClienteResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpfCnpj) {
        return clienteAssembler.toCollectionModel(clienteService.listar(nome, cpfCnpj));
    }

    @GetMapping("/{id}")
    public EntityModel<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return clienteAssembler.toModel(clienteService.buscarPorId(id));
    }

    /** @Valid dispara a Bean Validation do ClienteRequest; se falhar, o TratadorDeErros devolve 400. */
    @PostMapping
    public ResponseEntity<EntityModel<ClienteResponse>> cadastrar(@RequestBody @Valid ClienteRequest request) {
        EntityModel<ClienteResponse> model = clienteAssembler.toModel(clienteService.cadastrar(request));
        // Location: URI do recurso recém-criado (o próprio link "self"), ex.: http://localhost:8080/api/clientes/7
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<ClienteResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequest request) {
        return clienteAssembler.toModel(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }

    @GetMapping("/{id}/contratos")
    public CollectionModel<EntityModel<ContratoResponse>> listarContratos(@PathVariable Long id) {
        return contratoAssembler.toCollectionModel(contratoService.listarPorCliente(id));
    }
}
