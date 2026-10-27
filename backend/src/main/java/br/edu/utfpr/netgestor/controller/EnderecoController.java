package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.EnderecoRequest;
import br.edu.utfpr.netgestor.dto.EnderecoResponse;
import br.edu.utfpr.netgestor.service.EnderecoService;
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
 * Endereço é um sub-recurso do cliente:
 *   GET  /api/clientes/{clienteId}/enderecos   lista os endereços do cliente
 *   POST /api/clientes/{clienteId}/enderecos   cadastra um endereço para o cliente
 * Depois de criado, cada endereço tem a sua própria URI:
 *   GET/PUT/DELETE /api/enderecos/{id}
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/clientes/{clienteId}/enderecos")
    public List<EnderecoResponse> listarPorCliente(@PathVariable Long clienteId) {
        return enderecoService.listarPorCliente(clienteId);
    }

    @PostMapping("/clientes/{clienteId}/enderecos")
    public ResponseEntity<EnderecoResponse> cadastrar(@PathVariable Long clienteId,
                                                      @RequestBody @Valid EnderecoRequest request) {
        EnderecoResponse criado = enderecoService.cadastrar(clienteId, request);
        // O Location aponta para a URI própria do endereço, não para a do sub-recurso
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/enderecos/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/enderecos/{id}")
    public EnderecoResponse buscarPorId(@PathVariable Long id) {
        return enderecoService.buscarPorId(id);
    }

    @PutMapping("/enderecos/{id}")
    public EnderecoResponse atualizar(@PathVariable Long id, @RequestBody @Valid EnderecoRequest request) {
        return enderecoService.atualizar(id, request);
    }

    @DeleteMapping("/enderecos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        enderecoService.excluir(id);
    }
}
