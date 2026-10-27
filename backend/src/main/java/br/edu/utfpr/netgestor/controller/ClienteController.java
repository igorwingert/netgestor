package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.ClienteRequest;
import br.edu.utfpr.netgestor.dto.ClienteResponse;
import br.edu.utfpr.netgestor.service.ClienteService;
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
 * Recurso /api/clientes.
 *
 * O controller só cuida do HTTP (rota, verbo, status, validação de entrada);
 * as regras de negócio ficam no service. Todos os controllers seguem este padrão:
 *   GET    /api/clientes       200 lista
 *   GET    /api/clientes/{id}  200 ou 404
 *   POST   /api/clientes       201 Created + header Location (ou 400/409)
 *   PUT    /api/clientes/{id}  200 (ou 400/404/409)
 *   DELETE /api/clientes/{id}  204 No Content (ou 404/409)
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    /** @Valid dispara a Bean Validation do ClienteRequest; se falhar, o TratadorDeErros devolve 400. */
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request) {
        ClienteResponse criado = clienteService.cadastrar(request);
        // Location: URI do recurso recém-criado, ex.: http://localhost:8080/api/clientes/7
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequest request) {
        return clienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }
}
