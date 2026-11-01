package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.PagamentoAssembler;
import br.edu.utfpr.netgestor.dto.PagamentoRequest;
import br.edu.utfpr.netgestor.dto.PagamentoResponse;
import br.edu.utfpr.netgestor.service.PagamentoService;
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
 * Pagamento e um sub-recurso da fatura:
 *   GET  /api/faturas/{faturaId}/pagamentos
 *   POST /api/faturas/{faturaId}/pagamentos   (link "registrar-pagamento" da fatura)
 * e cada pagamento tem a sua URI propria: GET/PUT/DELETE /api/pagamentos/{id}
 */
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final PagamentoAssembler pagamentoAssembler;

    @GetMapping("/faturas/{faturaId}/pagamentos")
    public CollectionModel<EntityModel<PagamentoResponse>> listarPorFatura(@PathVariable Long faturaId) {
        return pagamentoAssembler.toCollectionModel(pagamentoService.listarPorFatura(faturaId));
    }

    @PostMapping("/faturas/{faturaId}/pagamentos")
    public ResponseEntity<EntityModel<PagamentoResponse>> cadastrar(@PathVariable Long faturaId,
                                                                    @RequestBody @Valid PagamentoRequest request) {
        EntityModel<PagamentoResponse> model = pagamentoAssembler.toModel(pagamentoService.cadastrar(faturaId, request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @GetMapping("/pagamentos/{id}")
    public EntityModel<PagamentoResponse> buscarPorId(@PathVariable Long id) {
        return pagamentoAssembler.toModel(pagamentoService.buscarPorId(id));
    }

    @PutMapping("/pagamentos/{id}")
    public EntityModel<PagamentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid PagamentoRequest request) {
        return pagamentoAssembler.toModel(pagamentoService.atualizar(id, request));
    }

    @DeleteMapping("/pagamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        pagamentoService.excluir(id);
    }
}
