package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.FaturaAssembler;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import br.edu.utfpr.netgestor.dto.FaturaRequest;
import br.edu.utfpr.netgestor.dto.FaturaResponse;
import br.edu.utfpr.netgestor.service.FaturaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/** Recurso /api/faturas, com filtros ?status=VENCIDA&competencia=2026-10 e a acao cancelar. */
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
@RestController
@RequestMapping("/api/faturas")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService faturaService;
    private final FaturaAssembler faturaAssembler;

    @GetMapping
    public CollectionModel<EntityModel<FaturaResponse>> listar(
            @RequestParam(required = false) StatusFatura status,
            @RequestParam(required = false) YearMonth competencia) {
        return faturaAssembler.toCollectionModel(faturaService.listar(status, competencia, null));
    }

    @GetMapping("/{id}")
    public EntityModel<FaturaResponse> buscarPorId(@PathVariable Long id) {
        return faturaAssembler.toModel(faturaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<FaturaResponse>> cadastrar(@RequestBody @Valid FaturaRequest request) {
        EntityModel<FaturaResponse> model = faturaAssembler.toModel(faturaService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<FaturaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid FaturaRequest request) {
        return faturaAssembler.toModel(faturaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        faturaService.excluir(id);
    }

    @PatchMapping("/{id}/cancelar")
    public EntityModel<FaturaResponse> cancelar(@PathVariable Long id) {
        return faturaAssembler.toModel(faturaService.cancelar(id));
    }
}
