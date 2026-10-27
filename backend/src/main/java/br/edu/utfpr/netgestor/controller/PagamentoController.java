package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.PagamentoRequest;
import br.edu.utfpr.netgestor.dto.PagamentoResponse;
import br.edu.utfpr.netgestor.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Pagamento é um sub-recurso da fatura:
 *   GET  /api/faturas/{faturaId}/pagamentos
 *   POST /api/faturas/{faturaId}/pagamentos
 * e cada pagamento tem a sua URI própria: GET/PUT/DELETE /api/pagamentos/{id}
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping("/faturas/{faturaId}/pagamentos")
    public List<PagamentoResponse> listarPorFatura(@PathVariable Long faturaId) {
        return pagamentoService.listarPorFatura(faturaId);
    }

    @PostMapping("/faturas/{faturaId}/pagamentos")
    public ResponseEntity<PagamentoResponse> cadastrar(@PathVariable Long faturaId,
                                                       @RequestBody @Valid PagamentoRequest request) {
        PagamentoResponse criado = pagamentoService.cadastrar(faturaId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/pagamentos/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/pagamentos/{id}")
    public PagamentoResponse buscarPorId(@PathVariable Long id) {
        return pagamentoService.buscarPorId(id);
    }

    @PutMapping("/pagamentos/{id}")
    public PagamentoResponse atualizar(@PathVariable Long id, @RequestBody @Valid PagamentoRequest request) {
        return pagamentoService.atualizar(id, request);
    }

    @DeleteMapping("/pagamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        pagamentoService.excluir(id);
    }
}
