package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.CtoAssembler;
import br.edu.utfpr.netgestor.assembler.OltAssembler;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import br.edu.utfpr.netgestor.dto.OltRequest;
import br.edu.utfpr.netgestor.dto.OltResponse;
import br.edu.utfpr.netgestor.service.CtoService;
import br.edu.utfpr.netgestor.service.OltService;
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

/** Recurso /api/olts e o sub-recurso /api/olts/{id}/ctos. */
@RestController
@RequestMapping("/api/olts")
@RequiredArgsConstructor
public class OltController {

    private final OltService oltService;
    private final OltAssembler oltAssembler;
    private final CtoService ctoService;
    private final CtoAssembler ctoAssembler;

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @GetMapping
    public CollectionModel<EntityModel<OltResponse>> listar() {
        return oltAssembler.toCollectionModel(oltService.listar());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @GetMapping("/{id}")
    public EntityModel<OltResponse> buscarPorId(@PathVariable Long id) {
        return oltAssembler.toModel(oltService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<OltResponse>> cadastrar(@RequestBody @Valid OltRequest request) {
        EntityModel<OltResponse> model = oltAssembler.toModel(oltService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EntityModel<OltResponse> atualizar(@PathVariable Long id, @RequestBody @Valid OltRequest request) {
        return oltAssembler.toModel(oltService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        oltService.excluir(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}/ctos")
    public CollectionModel<EntityModel<CtoResponse>> listarCtos(@PathVariable Long id) {
        return ctoAssembler.toCollectionModel(ctoService.listarPorOlt(id));
    }
}
