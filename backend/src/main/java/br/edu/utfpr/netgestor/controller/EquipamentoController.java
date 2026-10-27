package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.dto.EquipamentoRequest;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.service.EquipamentoService;
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

/** Recurso /api/equipamentos (ONUs e roteadores). Mesmo padrão do ClienteController. */
@RestController
@RequestMapping("/api/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    @GetMapping
    public List<EquipamentoResponse> listar() {
        return equipamentoService.listar();
    }

    @GetMapping("/{id}")
    public EquipamentoResponse buscarPorId(@PathVariable Long id) {
        return equipamentoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponse> cadastrar(@RequestBody @Valid EquipamentoRequest request) {
        EquipamentoResponse criado = equipamentoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public EquipamentoResponse atualizar(@PathVariable Long id, @RequestBody @Valid EquipamentoRequest request) {
        return equipamentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        equipamentoService.excluir(id);
    }
}
