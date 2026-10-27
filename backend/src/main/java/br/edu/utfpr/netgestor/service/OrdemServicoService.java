package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Chamado;
import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.Equipamento;
import br.edu.utfpr.netgestor.domain.OrdemServico;
import br.edu.utfpr.netgestor.domain.enums.StatusOrdemServico;
import br.edu.utfpr.netgestor.dto.OrdemServicoRequest;
import br.edu.utfpr.netgestor.dto.OrdemServicoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ContratoService contratoService;
    private final ChamadoService chamadoService;
    private final TecnicoService tecnicoService;
    private final EquipamentoService equipamentoService;

    /** Filtros opcionais: status, tecnicoId e contratoId (null = sem filtro). */
    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar(StatusOrdemServico status, Long tecnicoId, Long contratoId) {
        return ordemServicoRepository.buscar(status, tecnicoId, contratoId).stream()
                .map(OrdemServicoResponse::de)
                .toList();
    }

    /** Sub-recurso GET /api/tecnicos/{tecnicoId}/ordens-servico?status= (a agenda do técnico). */
    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorTecnico(Long tecnicoId, StatusOrdemServico status) {
        tecnicoService.buscarEntidade(tecnicoId); // 404 se o técnico não existir
        return listar(status, tecnicoId, null);
    }

    /** Sub-recurso GET /api/contratos/{contratoId}/ordens-servico. */
    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorContrato(Long contratoId) {
        contratoService.buscarEntidade(contratoId); // 404 se o contrato não existir
        return listar(null, null, contratoId);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(Long id) {
        return OrdemServicoResponse.de(buscarEntidade(id));
    }

    public OrdemServico buscarEntidade(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço " + id + " não encontrada"));
    }

    @Transactional
    public OrdemServicoResponse cadastrar(OrdemServicoRequest request) {
        OrdemServico os = new OrdemServico();
        os.setNumero(gerarNumero());
        copiarDados(request, os);
        return OrdemServicoResponse.de(ordemServicoRepository.save(os));
    }

    @Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
        OrdemServico os = buscarEntidade(id);
        if (!os.getStatus().podeEditar()) {
            throw new RegraNegocioException("A ordem de serviço " + os.getNumero() + " está "
                    + os.getStatus() + " e não pode ser alterada");
        }
        copiarDados(request, os);
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public void excluir(Long id) {
        OrdemServico os = buscarEntidade(id);
        if (os.getStatus() == StatusOrdemServico.CONCLUIDA) {
            throw new RegraNegocioException("A ordem de serviço " + os.getNumero()
                    + " está concluída e faz parte do histórico: não pode ser excluída");
        }
        // As linhas da tabela ordem_servico_equipamento são removidas junto
        ordemServicoRepository.delete(os);
    }

    // ---------------------------------------------------------------------
    // Ações: AGENDADA -> iniciar -> EM_EXECUCAO -> concluir -> CONCLUIDA
    //        (AGENDADA ou EM_EXECUCAO) -> cancelar -> CANCELADA
    // ---------------------------------------------------------------------

    @Transactional
    public OrdemServicoResponse iniciar(Long id) {
        OrdemServico os = buscarEntidade(id);
        validarAcao(os.getStatus().podeIniciar(), "iniciar", os);
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        return OrdemServicoResponse.de(os);
    }

    /** Ao concluir, registra a data/hora de conclusão. */
    @Transactional
    public OrdemServicoResponse concluir(Long id) {
        OrdemServico os = buscarEntidade(id);
        validarAcao(os.getStatus().podeConcluir(), "concluir", os);
        os.setStatus(StatusOrdemServico.CONCLUIDA);
        os.setDataConclusao(LocalDateTime.now());
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse cancelar(Long id) {
        OrdemServico os = buscarEntidade(id);
        validarAcao(os.getStatus().podeCancelar(), "cancelar", os);
        os.setStatus(StatusOrdemServico.CANCELADA);
        return OrdemServicoResponse.de(os);
    }

    private void validarAcao(boolean permitida, String acao, OrdemServico os) {
        if (!permitida) {
            throw new RegraNegocioException("Ação '" + acao + "' não permitida para a ordem de serviço "
                    + os.getNumero() + " com status " + os.getStatus());
        }
    }

    private void copiarDados(OrdemServicoRequest request, OrdemServico os) {
        Contrato contrato = contratoService.buscarEntidade(request.contratoId());
        Chamado chamado = request.chamadoId() == null ? null : chamadoService.buscarEntidade(request.chamadoId());
        if (chamado != null && !chamado.getContrato().getId().equals(contrato.getId())) {
            throw new RegraNegocioException("O chamado " + chamado.getProtocolo()
                    + " não pertence ao contrato " + contrato.getNumero());
        }

        // Monta o conjunto de equipamentos da OS (relacionamento N:N)
        Set<Equipamento> equipamentos = new HashSet<>();
        if (request.equipamentoIds() != null) {
            for (Long equipamentoId : request.equipamentoIds()) {
                equipamentos.add(equipamentoService.buscarEntidade(equipamentoId));
            }
        }

        os.setContrato(contrato);
        os.setChamado(chamado);
        os.setTecnico(tecnicoService.buscarEntidade(request.tecnicoId()));
        os.setTipo(request.tipo());
        os.setDataAgendada(request.dataAgendada());
        os.setObservacoes(request.observacoes());
        os.setEquipamentos(equipamentos);
    }

    /** Gera o próximo número do ano: OS-2026-0001, OS-2026-0002... */
    private String gerarNumero() {
        String prefixo = "OS-" + Year.now() + "-";
        long sequencia = ordemServicoRepository.countByNumeroStartingWith(prefixo) + 1;
        return prefixo + String.format("%04d", sequencia);
    }
}
