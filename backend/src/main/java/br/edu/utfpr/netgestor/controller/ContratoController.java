package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.ContratoRequest;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import br.edu.utfpr.netgestor.service.ContratoService;
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

/** Recurso /api/contratos (contratos de serviço). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public List<ContratoResponse> listar() {
        return contratoService.listar();
    }

    @GetMapping("/{id}")
    public ContratoResponse buscarPorId(@PathVariable Long id) {
        return contratoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ContratoResponse> cadastrar(@RequestBody @Valid ContratoRequest request) {
        ContratoResponse criado = contratoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public ContratoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ContratoRequest request) {
        return contratoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        contratoService.excluir(id);
    }
}
