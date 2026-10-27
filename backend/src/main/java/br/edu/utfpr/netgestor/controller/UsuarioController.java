package br.edu.utfpr.netgestor.controller;

import br.edu.utfpr.netgestor.assembler.UsuarioAssembler;
import br.edu.utfpr.netgestor.dto.UsuarioRequest;
import br.edu.utfpr.netgestor.dto.UsuarioResponse;
import br.edu.utfpr.netgestor.service.UsuarioService;
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

/** Recurso /api/usuarios (usuários do sistema). */
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAssembler usuarioAssembler;

    @GetMapping
    public CollectionModel<EntityModel<UsuarioResponse>> listar() {
        return usuarioAssembler.toCollectionModel(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public EntityModel<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return usuarioAssembler.toModel(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<UsuarioResponse>> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        EntityModel<UsuarioResponse> model = usuarioAssembler.toModel(usuarioService.cadastrar(request));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<UsuarioResponse> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequest request) {
        return usuarioAssembler.toModel(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
    }
}
