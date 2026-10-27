package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.PlanoRequest;
import br.edu.utfpr.netgestor.dto.PlanoResponse;
import br.edu.utfpr.netgestor.service.PlanoService;
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

/** Recurso /api/planos (planos de internet). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @GetMapping
    public List<PlanoResponse> listar() {
        return planoService.listar();
    }

    @GetMapping("/{id}")
    public PlanoResponse buscarPorId(@PathVariable Long id) {
        return planoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> cadastrar(@RequestBody @Valid PlanoRequest request) {
        PlanoResponse criado = planoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public PlanoResponse atualizar(@PathVariable Long id, @RequestBody @Valid PlanoRequest request) {
        return planoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        planoService.excluir(id);
    }
}
