package br.edu.utfpr.netgestor.assembler;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Utilitário dos assemblers. */
final class LinkUtil {

    private LinkUtil() {
    }

    /**
     * Link "self" de uma coleção: a própria URL chamada, incluindo os filtros.
     * Ex.: GET /api/contratos?status=ATIVO -> self = http://localhost:8080/api/contratos?status=ATIVO
     */
    static Link selfDaRequisicao() {
        return Link.of(ServletUriComponentsBuilder.fromCurrentRequest().toUriString(), IanaLinkRelations.SELF);
    }
}
