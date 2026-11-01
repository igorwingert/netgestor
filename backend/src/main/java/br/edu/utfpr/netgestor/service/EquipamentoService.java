package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.Equipamento;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.domain.enums.TipoEquipamento;
import br.edu.utfpr.netgestor.dto.EquipamentoRequest;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.EquipamentoRepository;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ContratoService contratoService;

    /** filtros opcionais: status, tipo e contratoId (null = sem filtro). */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listar(StatusEquipamento status, TipoEquipamento tipo, Long contratoId) {
        return equipamentoRepository.buscar(status, tipo, contratoId).stream()
                .map(EquipamentoResponse::de)
                .toList();
    }

    /** sub-recurso GET /api/contratos/{contratoId}/equipamentos (equipamentos em comodato). */
    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listarPorContrato(Long contratoId) {
        contratoService.buscarEntidade(contratoId); // 404 se o contrato nao existir
        return listar(null, null, contratoId);
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscarPorId(Long id) {
        return EquipamentoResponse.de(buscarEntidade(id));
    }

    public Equipamento buscarEntidade(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento " + id + " não encontrado"));
    }

    /** todo equipamento novo entra no ESTOQUE (o padrao da entidade). */
    @Transactional
    public EquipamentoResponse cadastrar(EquipamentoRequest request) {
        Equipamento equipamento = new Equipamento();
        copiarDados(request, equipamento);
        return EquipamentoResponse.de(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponse atualizar(Long id, EquipamentoRequest request) {
        Equipamento equipamento = buscarEntidade(id);
        copiarDados(request, equipamento);
        return EquipamentoResponse.de(equipamento);
    }

    @Transactional
    public void excluir(Long id) {
        Equipamento equipamento = buscarEntidade(id);
        if (equipamento.getStatus() == StatusEquipamento.INSTALADO) {
            throw new RegraNegocioException("O equipamento " + equipamento.getNumeroSerie()
                    + " está instalado no contrato " + equipamento.getContrato().getNumero()
                    + ". Desvincule-o antes de excluir.");
        }
        if (ordemServicoRepository.existsByEquipamentosId(id)) {
            throw new RegraNegocioException("O equipamento " + equipamento.getNumeroSerie()
                    + " aparece no histórico de ordens de serviço e não pode ser excluído");
        }
        equipamentoRepository.delete(equipamento);
    }

    // ---------------------------------------------------------------------
    // acoes de comodato:
    //   vincular    (ESTOQUE   -> INSTALADO): o equipamento passa a ficar com o contrato
    //   desvincular (INSTALADO -> ESTOQUE):   o equipamento volta para o estoque
    // ---------------------------------------------------------------------

    @Transactional
    public EquipamentoResponse vincular(Long id, Long contratoId) {
        Equipamento equipamento = buscarEntidade(id);
        validarAcao(equipamento.getStatus().podeVincular(), "vincular", equipamento);
        Contrato contrato = contratoService.buscarEntidade(contratoId);
        if (contrato.getStatus() == StatusContrato.CANCELADO) {
            throw new RegraNegocioException("O contrato " + contrato.getNumero()
                    + " está cancelado e não pode receber equipamentos");
        }
        equipamento.setContrato(contrato);
        equipamento.setStatus(StatusEquipamento.INSTALADO);
        return EquipamentoResponse.de(equipamento);
    }

    @Transactional
    public EquipamentoResponse desvincular(Long id) {
        Equipamento equipamento = buscarEntidade(id);
        validarAcao(equipamento.getStatus().podeDesvincular(), "desvincular", equipamento);
        equipamento.setContrato(null);
        equipamento.setStatus(StatusEquipamento.ESTOQUE);
        return EquipamentoResponse.de(equipamento);
    }

    private void validarAcao(boolean permitida, String acao, Equipamento equipamento) {
        if (!permitida) {
            throw new RegraNegocioException("Ação '" + acao + "' não permitida para o equipamento "
                    + equipamento.getNumeroSerie() + " com status " + equipamento.getStatus());
        }
    }

    private void copiarDados(EquipamentoRequest request, Equipamento equipamento) {
        equipamentoRepository.findByNumeroSerie(request.numeroSerie())
                .filter(outro -> !outro.getId().equals(equipamento.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um equipamento com o número de série "
                            + request.numeroSerie());
                });
        equipamentoRepository.findByMac(request.mac())
                .filter(outro -> !outro.getId().equals(equipamento.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um equipamento com o MAC " + request.mac());
                });

        equipamento.setTipo(request.tipo());
        equipamento.setFabricante(request.fabricante());
        equipamento.setModelo(request.modelo());
        equipamento.setNumeroSerie(request.numeroSerie());
        equipamento.setMac(request.mac());
    }
}
