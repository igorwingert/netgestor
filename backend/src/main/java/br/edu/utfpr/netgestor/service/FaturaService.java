package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.Fatura;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import br.edu.utfpr.netgestor.dto.FaturaRequest;
import br.edu.utfpr.netgestor.dto.FaturaResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.FaturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final ContratoService contratoService;

    /** filtros opcionais: status, competencia (mes, ex.: 2026-10) e contratoId (null = sem filtro). */
    @Transactional(readOnly = true)
    public List<FaturaResponse> listar(StatusFatura status, YearMonth competencia, Long contratoId) {
        LocalDate primeiroDia = competencia == null ? null : competencia.atDay(1);
        return faturaRepository.buscar(status, primeiroDia, contratoId).stream()
                .map(FaturaResponse::de)
                .toList();
    }

    /** sub-recurso GET /api/contratos/{contratoId}/faturas. */
    @Transactional(readOnly = true)
    public List<FaturaResponse> listarPorContrato(Long contratoId) {
        contratoService.buscarEntidade(contratoId); // 404 se o contrato nao existir
        return listar(null, null, contratoId);
    }

    @Transactional(readOnly = true)
    public FaturaResponse buscarPorId(Long id) {
        return FaturaResponse.de(buscarEntidade(id));
    }

    public Fatura buscarEntidade(Long id) {
        return faturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fatura " + id + " não encontrada"));
    }

    @Transactional
    public FaturaResponse cadastrar(FaturaRequest request) {
        Fatura fatura = new Fatura();
        copiarDados(request, fatura);
        return FaturaResponse.de(faturaRepository.save(fatura));
    }

    @Transactional
    public FaturaResponse atualizar(Long id, FaturaRequest request) {
        Fatura fatura = buscarEntidade(id);
        if (!fatura.getStatus().emAberto()) {
            throw new RegraNegocioException("A fatura " + id + " está " + fatura.getStatus()
                    + " e não pode ser alterada");
        }
        copiarDados(request, fatura);
        return FaturaResponse.de(fatura);
    }

    /** acao PATCH /api/faturas/{id}/cancelar: so fatura em aberto (ABERTA ou VENCIDA). */
    @Transactional
    public FaturaResponse cancelar(Long id) {
        Fatura fatura = buscarEntidade(id);
        if (!fatura.getStatus().emAberto()) {
            throw new RegraNegocioException("Ação 'cancelar' não permitida para a fatura " + id
                    + " com status " + fatura.getStatus());
        }
        fatura.setStatus(StatusFatura.CANCELADA);
        return FaturaResponse.de(fatura);
    }

    @Transactional
    public void excluir(Long id) {
        Fatura fatura = buscarEntidade(id);
        if (!fatura.getPagamentos().isEmpty()) {
            throw new RegraNegocioException("A fatura " + id
                    + " possui pagamentos e não pode ser excluída. Use a ação de cancelar.");
        }
        faturaRepository.delete(fatura);
    }

    private void copiarDados(FaturaRequest request, Fatura fatura) {
        Contrato contrato = contratoService.buscarEntidade(request.contratoId());
        if (contrato.getStatus() == StatusContrato.CANCELADO) {
            throw new RegraNegocioException("O contrato " + contrato.getNumero()
                    + " está cancelado e não pode ser faturado");
        }

        // a competencia e guardada como o 1º dia do mes (ex.: 2026-11 -> 2026-11-01)
        LocalDate competencia = request.competencia().atDay(1);
        faturaRepository.findByContratoIdAndCompetencia(contrato.getId(), competencia)
                .filter(outra -> !outra.getId().equals(fatura.getId()))
                .ifPresent(outra -> {
                    throw new RegraNegocioException("O contrato " + contrato.getNumero()
                            + " já possui fatura na competência " + request.competencia());
                });

        fatura.setContrato(contrato);
        fatura.setCompetencia(competencia);
        // valores padrao: mensalidade do plano e dia de vencimento do contrato
        fatura.setValor(request.valor() != null
                ? request.valor()
                : contrato.getPlano().getValorMensal());
        fatura.setDataVencimento(request.dataVencimento() != null
                ? request.dataVencimento()
                : request.competencia().atDay(contrato.getDiaVencimento()));
        fatura.atualizarStatus(LocalDate.now());
    }
}
