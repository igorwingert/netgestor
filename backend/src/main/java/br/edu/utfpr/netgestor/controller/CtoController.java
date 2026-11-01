package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.CtoAssembler;
import br.edu.utfpr.netgestor.dto.CtoRequest;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import br.edu.utfpr.netgestor.dto.OcupacaoCtoResponse;
import br.edu.utfpr.netgestor.service.CtoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Recurso /api/ctos e a consulta de ocupacao das portas: /api/ctos/{id}/ocupacao */
@RestController
@RequestMapping("/api/ctos")
@RequiredArgsConstructor
public class CtoController {

    private final CtoService ctoService;
    private final CtoAssembler ctoAssembler;

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping
    public CollectionModel<EntityModel<CtoResponse>> listar() {
        return ctoAssembler.toCollectionModel(ctoService.listar());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}")
    public EntityModel<CtoResponse> buscarPorId(@PathVariable Long id) {
        return ctoAssembler.toModel(ctoService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<CtoResponse>> cadastrar(@RequestBody @Valid CtoRequest request) {
        EntityModel<CtoResponse> model = ctoAssembler.toModel(ctoService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EntityModel<CtoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid CtoRequest request) {
        return ctoAssembler.toModel(ctoService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ctoService.excluir(id);
    }

    /** portas usadas e livres da CTO. nao e um recurso com CRUD, entao os links ficam aqui mesmo. */
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}/ocupacao")
    public EntityModel<OcupacaoCtoResponse> ocupacao(@PathVariable Long id) {
        return EntityModel.of(ctoService.ocupacao(id),
                linkTo(methodOn(CtoController.class).ocupacao(id)).withSelfRel(),
                linkTo(methodOn(CtoController.class).buscarPorId(id)).withRel("cto"));
    }
}
