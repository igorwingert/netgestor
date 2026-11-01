package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.ChamadoAssembler;
import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import br.edu.utfpr.netgestor.dto.ChamadoRequest;
import br.edu.utfpr.netgestor.dto.ChamadoResponse;
import br.edu.utfpr.netgestor.security.UsuarioLogado;
import br.edu.utfpr.netgestor.service.ChamadoService;
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

/** Recurso /api/chamados, com filtros ?status=&prioridade= e as acoes do ciclo de vida. */
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final ChamadoAssembler chamadoAssembler;

    @GetMapping
    public CollectionModel<EntityModel<ChamadoResponse>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade) {
        return chamadoAssembler.toCollectionModel(chamadoService.listar(status, prioridade, null));
    }

    @GetMapping("/{id}")
    public EntityModel<ChamadoResponse> buscarPorId(@PathVariable Long id) {
        return chamadoAssembler.toModel(chamadoService.buscarPorId(id));
    }

    @PostMapping
    /** quem abre o chamado e o usuario dono do token (@AuthenticationPrincipal). */
    public ResponseEntity<EntityModel<ChamadoResponse>> cadastrar(@RequestBody @Valid ChamadoRequest request,
                                                                  @AuthenticationPrincipal UsuarioLogado usuario) {
        EntityModel<ChamadoResponse> model = chamadoAssembler.toModel(chamadoService.cadastrar(request, usuario.getId()));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<ChamadoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ChamadoRequest request) {
        return chamadoAssembler.toModel(chamadoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        chamadoService.excluir(id);
    }

    @PatchMapping("/{id}/iniciar-atendimento")
    public EntityModel<ChamadoResponse> iniciarAtendimento(@PathVariable Long id) {
        return chamadoAssembler.toModel(chamadoService.iniciarAtendimento(id));
    }

    @PatchMapping("/{id}/resolver")
    public EntityModel<ChamadoResponse> resolver(@PathVariable Long id) {
        return chamadoAssembler.toModel(chamadoService.resolver(id));
    }

    @PatchMapping("/{id}/fechar")
    public EntityModel<ChamadoResponse> fechar(@PathVariable Long id) {
        return chamadoAssembler.toModel(chamadoService.fechar(id));
    }
}
