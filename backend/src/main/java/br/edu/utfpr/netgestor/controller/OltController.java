package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.OltRequest;
import br.edu.utfpr.netgestor.dto.OltResponse;
import br.edu.utfpr.netgestor.service.OltService;
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

/** Recurso /api/olts (OLTs da rede óptica). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/olts")
@RequiredArgsConstructor
public class OltController {

    private final OltService oltService;

    @GetMapping
    public List<OltResponse> listar() {
        return oltService.listar();
    }

    @GetMapping("/{id}")
    public OltResponse buscarPorId(@PathVariable Long id) {
        return oltService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<OltResponse> cadastrar(@RequestBody @Valid OltRequest request) {
        OltResponse criado = oltService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public OltResponse atualizar(@PathVariable Long id, @RequestBody @Valid OltRequest request) {
        return oltService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        oltService.excluir(id);
    }
}
