package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.TecnicoRequest;
import br.edu.utfpr.netgestor.dto.TecnicoResponse;
import br.edu.utfpr.netgestor.service.TecnicoService;
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

/** Recurso /api/tecnicos (técnicos de campo). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @GetMapping
    public List<TecnicoResponse> listar() {
        return tecnicoService.listar();
    }

    @GetMapping("/{id}")
    public TecnicoResponse buscarPorId(@PathVariable Long id) {
        return tecnicoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TecnicoResponse> cadastrar(@RequestBody @Valid TecnicoRequest request) {
        TecnicoResponse criado = tecnicoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public TecnicoResponse atualizar(@PathVariable Long id, @RequestBody @Valid TecnicoRequest request) {
        return tecnicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        tecnicoService.excluir(id);
    }
}
