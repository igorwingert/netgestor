package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.FaturaRequest;
import br.edu.utfpr.netgestor.dto.FaturaResponse;
import br.edu.utfpr.netgestor.service.FaturaService;
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

/** Recurso /api/faturas (faturas mensais). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/faturas")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService faturaService;

    @GetMapping
    public List<FaturaResponse> listar() {
        return faturaService.listar();
    }

    @GetMapping("/{id}")
    public FaturaResponse buscarPorId(@PathVariable Long id) {
        return faturaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<FaturaResponse> cadastrar(@RequestBody @Valid FaturaRequest request) {
        FaturaResponse criado = faturaService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public FaturaResponse atualizar(@PathVariable Long id, @RequestBody @Valid FaturaRequest request) {
        return faturaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        faturaService.excluir(id);
    }
}
