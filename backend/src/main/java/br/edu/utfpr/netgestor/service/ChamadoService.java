package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Chamado;
import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.dto.ChamadoRequest;
import br.edu.utfpr.netgestor.dto.ChamadoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ChamadoRepository;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ContratoService contratoService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar() {
        return chamadoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataAbertura")).stream()
                .map(ChamadoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return ChamadoResponse.de(buscarEntidade(id));
    }

    public Chamado buscarEntidade(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado " + id + " não encontrado"));
    }

    @Transactional
    public ChamadoResponse cadastrar(ChamadoRequest request) {
        Chamado chamado = new Chamado();
        chamado.setProtocolo(gerarProtocolo());
        chamado.setAbertoPor(usuarioService.buscarEntidade(request.abertoPorId()));
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
