package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.PlanoAssembler;
import br.edu.utfpr.netgestor.dto.PlanoRequest;
import br.edu.utfpr.netgestor.dto.PlanoResponse;
import br.edu.utfpr.netgestor.service.PlanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
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

/** Recurso /api/planos (planos de internet). */
@RestController
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;
    private final PlanoAssembler planoAssembler;

    @GetMapping
    public CollectionModel<EntityModel<PlanoResponse>> listar() {
        return planoAssembler.toCollectionModel(planoService.listar());
    }

    @GetMapping("/{id}")
    public EntityModel<PlanoResponse> buscarPorId(@PathVariable Long id) {
        return planoAssembler.toModel(planoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<PlanoResponse>> cadastrar(@RequestBody @Valid PlanoRequest request) {
        EntityModel<PlanoResponse> model = planoAssembler.toModel(planoService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<PlanoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid PlanoRequest request) {
        return planoAssembler.toModel(planoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        planoService.excluir(id);
    }
}
