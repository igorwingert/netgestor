package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.ChamadoAssembler;
import br.edu.utfpr.netgestor.assembler.ContratoAssembler;
import br.edu.utfpr.netgestor.assembler.EquipamentoAssembler;
import br.edu.utfpr.netgestor.assembler.FaturaAssembler;
import br.edu.utfpr.netgestor.assembler.OrdemServicoAssembler;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.dto.ChamadoResponse;
import br.edu.utfpr.netgestor.dto.ContratoRequest;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.dto.FaturaResponse;
import br.edu.utfpr.netgestor.dto.OrdemServicoResponse;
import br.edu.utfpr.netgestor.service.ChamadoService;
import br.edu.utfpr.netgestor.service.ContratoService;
import br.edu.utfpr.netgestor.service.EquipamentoService;
import br.edu.utfpr.netgestor.service.FaturaService;
import br.edu.utfpr.netgestor.service.OrdemServicoService;
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

/**
 * Recurso /api/contratos: o mais completo da API.
 *
 *   GET    /api/contratos?status=&clienteId=         lista com filtros opcionais
 *   GET    /api/contratos/{id}                       busca por id
 *   POST   /api/contratos                            201 + Location
 *   PUT    /api/contratos/{id}                       atualiza
 *   DELETE /api/contratos/{id}                       204
 *   PATCH  /api/contratos/{id}/suspender|reativar|cancelar   ações de status
 *   GET    /api/contratos/{id}/faturas|chamados|ordens-servico|equipamentos   sub-recursos
 *
 * As respostas são EntityModel/CollectionModel: o assembler acrescenta os links (HAL).
 */
@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;
    private final ContratoAssembler contratoAssembler;
    private final FaturaService faturaService;
    private final FaturaAssembler faturaAssembler;
    private final ChamadoService chamadoService;
    private final ChamadoAssembler chamadoAssembler;
    private final OrdemServicoService ordemServicoService;
    private final OrdemServicoAssembler ordemServicoAssembler;
    private final EquipamentoService equipamentoService;
    private final EquipamentoAssembler equipamentoAssembler;

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping
    public CollectionModel<EntityModel<ContratoResponse>> listar(
            @RequestParam(required = false) StatusContrato status,
            @RequestParam(required = false) Long clienteId) {
        return contratoAssembler.toCollectionModel(contratoService.listar(status, clienteId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}")
    public EntityModel<ContratoResponse> buscarPorId(@PathVariable Long id) {
        return contratoAssembler.toModel(contratoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping
    public ResponseEntity<EntityModel<ContratoResponse>> cadastrar(@RequestBody @Valid ContratoRequest request) {
        EntityModel<ContratoResponse> model = contratoAssembler.toModel(contratoService.cadastrar(request));
        // O header Location é o próprio link "self" gerado pelo assembler
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PutMapping("/{id}")
    public EntityModel<ContratoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ContratoRequest request) {
        return contratoAssembler.toModel(contratoService.atualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        contratoService.excluir(id);
    }

    // ---- Ações (PATCH): devolvem o contrato com o novo status e os novos links ----

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/suspender")
    public EntityModel<ContratoResponse> suspender(@PathVariable Long id) {
        return contratoAssembler.toModel(contratoService.suspender(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/reativar")
    public EntityModel<ContratoResponse> reativar(@PathVariable Long id) {
        return contratoAssembler.toModel(contratoService.reativar(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PatchMapping("/{id}/cancelar")
    public EntityModel<ContratoResponse> cancelar(@PathVariable Long id) {
        return contratoAssembler.toModel(contratoService.cancelar(id));
    }

    // ---- Sub-recursos do contrato ----

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @GetMapping("/{id}/faturas")
    public CollectionModel<EntityModel<FaturaResponse>> listarFaturas(@PathVariable Long id) {
        return faturaAssembler.toCollectionModel(faturaService.listarPorContrato(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @GetMapping("/{id}/chamados")
    public CollectionModel<EntityModel<ChamadoResponse>> listarChamados(@PathVariable Long id) {
        return chamadoAssembler.toCollectionModel(chamadoService.listarPorContrato(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}/ordens-servico")
    public CollectionModel<EntityModel<OrdemServicoResponse>> listarOrdensServico(@PathVariable Long id) {
        return ordemServicoAssembler.toCollectionModel(ordemServicoService.listarPorContrato(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    @GetMapping("/{id}/equipamentos")
    public CollectionModel<EntityModel<EquipamentoResponse>> listarEquipamentos(@PathVariable Long id) {
        return equipamentoAssembler.toCollectionModel(equipamentoService.listarPorContrato(id));
    }
}
