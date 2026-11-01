package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.OrdemServicoAssembler;
import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import br.edu.utfpr.netgestor.dto.OrdemServicoRequest;
import br.edu.utfpr.netgestor.dto.OrdemServicoResponse;
import br.edu.utfpr.netgestor.security.UsuarioLogado;
import br.edu.utfpr.netgestor.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recurso /api/ordens-servico, com filtros ?status=&tecnicoId= e as acoes iniciar/concluir/cancelar. */
@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;
    private final OrdemServicoAssembler ordemServicoAssembler;

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping
    public CollectionModel<EntityModel<OrdemServicoResponse>> listar(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(required = false) Long tecnicoId) {
        return ordemServicoAssembler.toCollectionModel(ordemServicoService.listar(status, tecnicoId, null));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}")
    public EntityModel<OrdemServicoResponse> buscarPorId(@PathVariable Long id) {
        return ordemServicoAssembler.toModel(ordemServicoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping
    public ResponseEntity<EntityModel<OrdemServicoResponse>> cadastrar(@RequestBody @Valid OrdemServicoRequest request) {
        EntityModel<OrdemServicoResponse> model = ordemServicoAssembler.toModel(ordemServicoService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/{id}")
    public EntityModel<OrdemServicoResponse> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid OrdemServicoRequest request) {
        return ordemServicoAssembler.toModel(ordemServicoService.atualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ordemServicoService.excluir(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PatchMapping("/{id}/iniciar")
    public EntityModel<OrdemServicoResponse> iniciar(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioLogado usuario) {
        return ordemServicoAssembler.toModel(ordemServicoService.iniciar(id, usuario));
    }

    /** alem do perfil, o service confere se a OS e do tecnico logado (senao, 403). */
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    @PatchMapping("/{id}/concluir")
    public EntityModel<OrdemServicoResponse> concluir(@PathVariable Long id,
                                                      @AuthenticationPrincipal UsuarioLogado usuario) {
        return ordemServicoAssembler.toModel(ordemServicoService.concluir(id, usuario));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/cancelar")
    public EntityModel<OrdemServicoResponse> cancelar(@PathVariable Long id) {
        return ordemServicoAssembler.toModel(ordemServicoService.cancelar(id));
    }
}
