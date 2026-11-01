package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.OrdemServicoAssembler;
import br.edu.utfpr.netgestor.assembler.TecnicoAssembler;
import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import br.edu.utfpr.netgestor.dto.OrdemServicoResponse;
import br.edu.utfpr.netgestor.dto.TecnicoRequest;
import br.edu.utfpr.netgestor.dto.TecnicoResponse;
import br.edu.utfpr.netgestor.service.OrdemServicoService;
import br.edu.utfpr.netgestor.service.TecnicoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recurso /api/tecnicos e a agenda de cada tecnico: /api/tecnicos/{id}/ordens-servico?status= */
@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;
    private final TecnicoAssembler tecnicoAssembler;
    private final OrdemServicoService ordemServicoService;
    private final OrdemServicoAssembler ordemServicoAssembler;

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping
    public CollectionModel<EntityModel<TecnicoResponse>> listar() {
        return tecnicoAssembler.toCollectionModel(tecnicoService.listar());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}")
    public EntityModel<TecnicoResponse> buscarPorId(@PathVariable Long id) {
        return tecnicoAssembler.toModel(tecnicoService.buscarPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<TecnicoResponse>> cadastrar(@RequestBody @Valid TecnicoRequest request) {
        EntityModel<TecnicoResponse> model = tecnicoAssembler.toModel(tecnicoService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EntityModel<TecnicoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid TecnicoRequest request) {
        return tecnicoAssembler.toModel(tecnicoService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        tecnicoService.excluir(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}/ordens-servico")
    public CollectionModel<EntityModel<OrdemServicoResponse>> listarOrdensServico(
            @PathVariable Long id,
            @RequestParam(required = false) StatusOrdemServico status) {
        return ordemServicoAssembler.toCollectionModel(ordemServicoService.listarPorTecnico(id, status));
    }
}
