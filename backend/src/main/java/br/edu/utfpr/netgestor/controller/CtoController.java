package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.CtoRequest;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import br.edu.utfpr.netgestor.service.CtoService;
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

/** Recurso /api/ctos (caixas de terminação óptica). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/ctos")
@RequiredArgsConstructor
public class CtoController {

    private final CtoService ctoService;

    @GetMapping
    public List<CtoResponse> listar() {
        return ctoService.listar();
    }

    @GetMapping("/{id}")
    public CtoResponse buscarPorId(@PathVariable Long id) {
        return ctoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<CtoResponse> cadastrar(@RequestBody @Valid CtoRequest request) {
        CtoResponse criado = ctoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public CtoResponse atualizar(@PathVariable Long id, @RequestBody @Valid CtoRequest request) {
        return ctoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ctoService.excluir(id);
    }
}
