package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Equipamento;
import br.edu.utfpr.netgestor.domain.enums.StatusEquipamento;
import br.edu.utfpr.netgestor.dto.EquipamentoRequest;
import br.edu.utfpr.netgestor.dto.EquipamentoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.EquipamentoRepository;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listar() {
        return equipamentoRepository.findAll(Sort.by("id")).stream()
                .map(EquipamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscarPorId(Long id) {
        return EquipamentoResponse.de(buscarEntidade(id));
    }

    public Equipamento buscarEntidade(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento " + id + " não encontrado"));
    }

    /** Todo equipamento novo entra no ESTOQUE (o padrão da entidade). */
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
