package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.EquipamentoAssembler;
import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import br.edu.utfpr.netgestor.dto.EquipamentoRequest;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.dto.VinculoEquipamentoRequest;
import br.edu.utfpr.netgestor.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/** Recurso /api/equipamentos, com filtros ?status=&tipo= e as ações de comodato vincular/desvincular. */
@RestController
@RequestMapping("/api/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService equipamentoService;
    private final EquipamentoAssembler equipamentoAssembler;

    @GetMapping
    public CollectionModel<EntityModel<EquipamentoResponse>> listar(
            @RequestParam(required = false) StatusEquipamento status,
            @RequestParam(required = false) TipoEquipamento tipo) {
        return equipamentoAssembler.toCollectionModel(equipamentoService.listar(status, tipo, null));
    }

    @GetMapping("/{id}")
    public EntityModel<EquipamentoResponse> buscarPorId(@PathVariable Long id) {
        return equipamentoAssembler.toModel(equipamentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<EquipamentoResponse>> cadastrar(@RequestBody @Valid EquipamentoRequest request) {
        EntityModel<EquipamentoResponse> model = equipamentoAssembler.toModel(equipamentoService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<EquipamentoResponse> atualizar(@PathVariable Long id,
                                                      @RequestBody @Valid EquipamentoRequest request) {
        return equipamentoAssembler.toModel(equipamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        equipamentoService.excluir(id);
    }

    /** Corpo: { "contratoId": 7 } */
    @PatchMapping("/{id}/vincular")
    public EntityModel<EquipamentoResponse> vincular(@PathVariable Long id,
                                                     @RequestBody @Valid VinculoEquipamentoRequest request) {
        return equipamentoAssembler.toModel(equipamentoService.vincular(id, request.contratoId()));
    }

    @PatchMapping("/{id}/desvincular")
    public EntityModel<EquipamentoResponse> desvincular(@PathVariable Long id) {
        return equipamentoAssembler.toModel(equipamentoService.desvincular(id));
    }
}
