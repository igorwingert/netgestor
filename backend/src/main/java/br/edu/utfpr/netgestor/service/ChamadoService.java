package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Chamado;
import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.dto.ChamadoRequest;
import br.edu.utfpr.netgestor.dto.ChamadoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ChamadoRepository;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ContratoService contratoService;
    private final UsuarioService usuarioService;

    /** Filtros opcionais: status, prioridade e contratoId (null = sem filtro). */
    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade, Long contratoId) {
        return chamadoRepository.buscar(status, prioridade, contratoId).stream()
                .map(ChamadoResponse::de)
                .toList();
    }

    /** Sub-recurso GET /api/contratos/{contratoId}/chamados. */
    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarPorContrato(Long contratoId) {
        contratoService.buscarEntidade(contratoId); // 404 se o contrato não existir
        return listar(null, null, contratoId);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return ChamadoResponse.de(buscarEntidade(id));
    }

    public Chamado buscarEntidade(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado " + id + " não encontrado"));
    }

    /** abertoPorId = id do usuário logado (vem do token, via controller). */
    @Transactional
    public ChamadoResponse cadastrar(ChamadoRequest request, Long abertoPorId) {
        Chamado chamado = new Chamado();
        chamado.setProtocolo(gerarProtocolo());
        chamado.setAbertoPor(usuarioService.buscarEntidade(abertoPorId));
        copiarDados(request, chamado);
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    /** Atualiza contrato, tipo, prioridade e descrição. Quem abriu o chamado não muda. */
    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = buscarEntidade(id);
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("O chamado " + chamado.getProtocolo()
                    + " está fechado e não pode ser alterado");
        }
        copiarDados(request, chamado);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        Chamado chamado = buscarEntidade(id);
        if (ordemServicoRepository.existsByChamadoId(id)) {
            throw new RegraNegocioException("O chamado " + chamado.getProtocolo()
                    + " originou ordens de serviço e não pode ser excluído");
        }
        chamadoRepository.delete(chamado);
    }

    // ---------------------------------------------------------------------
    // Ações: ABERTO -> iniciar-atendimento -> EM_ATENDIMENTO -> resolver -> RESOLVIDO -> fechar -> FECHADO
    // ---------------------------------------------------------------------

    @Transactional
    public ChamadoResponse iniciarAtendimento(Long id) {
        Chamado chamado = buscarEntidade(id);
        validarAcao(chamado.getStatus().podeIniciarAtendimento(), "iniciar-atendimento", chamado);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse resolver(Long id) {
        Chamado chamado = buscarEntidade(id);
        validarAcao(chamado.getStatus().podeResolver(), "resolver", chamado);
        chamado.setStatus(StatusChamado.RESOLVIDO);
        return ChamadoResponse.de(chamado);
    }

    /** Ao fechar, registra a data/hora de fechamento. */
    @Transactional
    public ChamadoResponse fechar(Long id) {
        Chamado chamado = buscarEntidade(id);
        validarAcao(chamado.getStatus().podeFechar(), "fechar", chamado);
        chamado.setStatus(StatusChamado.FECHADO);
        chamado.setDataFechamento(LocalDateTime.now());
        return ChamadoResponse.de(chamado);
    }

    private void validarAcao(boolean permitida, String acao, Chamado chamado) {
        if (!permitida) {
            throw new RegraNegocioException("Ação '" + acao + "' não permitida para o chamado "
                    + chamado.getProtocolo() + " com status " + chamado.getStatus());
        }
    }

    private void copiarDados(ChamadoRequest request, Chamado chamado) {
        Contrato contrato = contratoService.buscarEntidade(request.contratoId());
        if (contrato.getStatus() == StatusContrato.CANCELADO) {
            throw new RegraNegocioException("O contrato " + contrato.getNumero()
                    + " está cancelado e não aceita chamados");
        }
        chamado.setContrato(contrato);
        chamado.setTipo(request.tipo());
        chamado.setPrioridade(request.prioridade());
        chamado.setDescricao(request.descricao());
    }

    /** Gera o próximo protocolo do ano: CH-2026-000001, CH-2026-000002... */
    private String gerarProtocolo() {
        String prefixo = "CH-" + Year.now() + "-";
        long sequencia = chamadoRepository.countByProtocoloStartingWith(prefixo) + 1;
        return prefixo + String.format("%06d", sequencia);
    }
}
