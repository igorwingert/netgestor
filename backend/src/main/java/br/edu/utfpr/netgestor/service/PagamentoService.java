package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Fatura;
import br.edu.utfpr.netgestor.domain.Pagamento;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import br.edu.utfpr.netgestor.dto.PagamentoRequest;
import br.edu.utfpr.netgestor.dto.PagamentoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Pagamentos são um sub-recurso da fatura: /api/faturas/{faturaId}/pagamentos.
 * Toda alteração de pagamento recalcula o status da fatura (regra em Fatura.atualizarStatus).
 */
@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final FaturaService faturaService;

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorFatura(Long faturaId) {
        faturaService.buscarEntidade(faturaId); // 404 se a fatura não existir
        return pagamentoRepository.findByFaturaIdOrderByDataPagamento(faturaId).stream()
                .map(PagamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorId(Long id) {
        return PagamentoResponse.de(buscarEntidade(id));
    }

    public Pagamento buscarEntidade(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento " + id + " não encontrado"));
    }

    @Transactional
    public PagamentoResponse cadastrar(Long faturaId, PagamentoRequest request) {
        Fatura fatura = faturaService.buscarEntidade(faturaId);
        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RegraNegocioException("A fatura " + faturaId + " já está paga");
        }
        if (fatura.getStatus() == StatusFatura.CANCELADA) {
            throw new RegraNegocioException("A fatura " + faturaId + " está cancelada e não aceita pagamentos");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setFatura(fatura);
        copiarDados(request, pagamento);
        fatura.getPagamentos().add(pagamento);

        // Se a soma dos pagamentos alcançou o valor da fatura, ela passa para PAGA
        fatura.atualizarStatus(LocalDate.now());
        return PagamentoResponse.de(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse atualizar(Long id, PagamentoRequest request) {
        Pagamento pagamento = buscarEntidade(id);
        validarFaturaNaoCancelada(pagamento.getFatura());
        copiarDados(request, pagamento);
        pagamento.getFatura().atualizarStatus(LocalDate.now());
        return PagamentoResponse.de(pagamento);
    }

    @Transactional
    public void excluir(Long id) {
        Pagamento pagamento = buscarEntidade(id);
        Fatura fatura = pagamento.getFatura();
        validarFaturaNaoCancelada(fatura);
        fatura.getPagamentos().remove(pagamento);
        pagamentoRepository.delete(pagamento);
        // Sem esse pagamento a fatura pode voltar a ABERTA ou VENCIDA
        fatura.atualizarStatus(LocalDate.now());
    }

    private void validarFaturaNaoCancelada(Fatura fatura) {
        if (fatura.getStatus() == StatusFatura.CANCELADA) {
            throw new RegraNegocioException("A fatura " + fatura.getId()
                    + " está cancelada: seus pagamentos não podem ser alterados");
        }
    }

    private void copiarDados(PagamentoRequest request, Pagamento pagamento) {
        pagamento.setDataPagamento(request.dataPagamento());
        pagamento.setValorPago(request.valorPago());
        pagamento.setForma(request.forma());
    }
}
