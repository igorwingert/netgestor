package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.EnderecoAssembler;
import br.edu.utfpr.netgestor.dto.EnderecoRequest;
import br.edu.utfpr.netgestor.dto.EnderecoResponse;
import br.edu.utfpr.netgestor.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * endereco e um sub-recurso do cliente:
 *   GET  /api/clientes/{clienteId}/enderecos   lista os enderecos do cliente
 *   POST /api/clientes/{clienteId}/enderecos   cadastra um endereco para o cliente
 * depois de criado, cada endereco tem a sua propria URI:
 *   GET/PUT/DELETE /api/enderecos/{id}
 */
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;
    private final EnderecoAssembler enderecoAssembler;

    @GetMapping("/clientes/{clienteId}/enderecos")
    public CollectionModel<EntityModel<EnderecoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return enderecoAssembler.toCollectionModel(enderecoService.listarPorCliente(clienteId));
    }

    @PostMapping("/clientes/{clienteId}/enderecos")
    public ResponseEntity<EntityModel<EnderecoResponse>> cadastrar(@PathVariable Long clienteId,
                                                                   @RequestBody @Valid EnderecoRequest request) {
        EntityModel<EnderecoResponse> model = enderecoAssembler.toModel(enderecoService.cadastrar(clienteId, request));
        // o Location aponta para a URI propria do endereco (/api/enderecos/{id})
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/enderecos/{id}")
    public EntityModel<EnderecoResponse> buscarPorId(@PathVariable Long id) {
        return enderecoAssembler.toModel(enderecoService.buscarPorId(id));
    }

    @PutMapping("/enderecos/{id}")
    public EntityModel<EnderecoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid EnderecoRequest request) {
        return enderecoAssembler.toModel(enderecoService.atualizar(id, request));
    }

    @DeleteMapping("/enderecos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        enderecoService.excluir(id);
    }
}
