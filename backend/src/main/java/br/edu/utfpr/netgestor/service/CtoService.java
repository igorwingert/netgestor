package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.Cto;
import br.edu.utfpr.netgestor.domain.Olt;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.dto.CtoRequest;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import br.edu.utfpr.netgestor.dto.OcupacaoCtoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import br.edu.utfpr.netgestor.repository.CtoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CtoService {

    private final CtoRepository ctoRepository;
    private final ContratoRepository contratoRepository;
    private final OltService oltService;

    @Transactional(readOnly = true)
    public List<CtoResponse> listar() {
        return ctoRepository.findAll(Sort.by("identificacao")).stream()
                .map(CtoResponse::de)
                .toList();
    }

    /** Sub-recurso GET /api/olts/{oltId}/ctos. */
    @Transactional(readOnly = true)
    public List<CtoResponse> listarPorOlt(Long oltId) {
        oltService.buscarEntidade(oltId); // 404 se a OLT não existir
        return ctoRepository.findByOltIdOrderByIdentificacao(oltId).stream()
                .map(CtoResponse::de)
                .toList();
    }

    /** GET /api/ctos/{id}/ocupacao: lista as portas de 1 até a capacidade, indicando quem ocupa cada uma. */
    @Transactional(readOnly = true)
    public OcupacaoCtoResponse ocupacao(Long id) {
        Cto cto = buscarEntidade(id);

        // porta -> contrato que a ocupa (contratos cancelados não ocupam porta)
        Map<Integer, Contrato> ocupadas = new HashMap<>();
        for (Contrato contrato : contratoRepository.findByCtoIdAndStatusNotOrderByPortaCto(id, StatusContrato.CANCELADO)) {
            ocupadas.put(contrato.getPortaCto(), contrato);
        }

        List<OcupacaoCtoResponse.Porta> portas = new ArrayList<>();
        for (int numero = 1; numero <= cto.getCapacidade(); numero++) {
            Contrato contrato = ocupadas.get(numero);
            portas.add(contrato == null
                    ? new OcupacaoCtoResponse.Porta(numero, true, null, null, null)
                    : new OcupacaoCtoResponse.Porta(numero, false, contrato.getId(),
                            contrato.getNumero(), contrato.getCliente().getNome()));
        }
        return new OcupacaoCtoResponse(cto.getId(), cto.getIdentificacao(), cto.getCapacidade(),
                ocupadas.size(), cto.getCapacidade() - ocupadas.size(), portas);
    }

    @Transactional(readOnly = true)
    public CtoResponse buscarPorId(Long id) {
        return CtoResponse.de(buscarEntidade(id));
    }

    public Cto buscarEntidade(Long id) {
        return ctoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("CTO " + id + " não encontrada"));
    }

    @Transactional
    public CtoResponse cadastrar(CtoRequest request) {
        Cto cto = new Cto();
        copiarDados(request, cto);
        return CtoResponse.de(ctoRepository.save(cto));
    }

    @Transactional
    public CtoResponse atualizar(Long id, CtoRequest request) {
        Cto cto = buscarEntidade(id);
        // Não deixa reduzir a capacidade abaixo de uma porta que já tem cliente
        if (contratoRepository.existsByCtoIdAndPortaCtoGreaterThanAndStatusNot(
                id, request.capacidade(), StatusContrato.CANCELADO)) {
            throw new RegraNegocioException("Existem contratos em portas acima de "
                    + request.capacidade() + " nesta CTO");
        }
        copiarDados(request, cto);
        return CtoResponse.de(cto);
    }

    @Transactional
    public void excluir(Long id) {
        Cto cto = buscarEntidade(id);
        if (contratoRepository.existsByCtoId(id)) {
            throw new RegraNegocioException("A CTO " + cto.getIdentificacao()
                    + " possui contratos e não pode ser excluída");
        }
        ctoRepository.delete(cto);
    }

    private void copiarDados(CtoRequest request, Cto cto) {
        ctoRepository.findByIdentificacaoIgnoreCase(request.identificacao())
                .filter(outra -> !outra.getId().equals(cto.getId()))
                .ifPresent(outra -> {
                    throw new RegraNegocioException("Já existe uma CTO com a identificação " + request.identificacao());
                });

        Olt olt = oltService.buscarEntidade(request.oltId());
        if (request.portaPon() > olt.getTotalPortasPon()) {
            throw new RegraNegocioException("A OLT " + olt.getNome() + " tem apenas "
                    + olt.getTotalPortasPon() + " portas PON (porta informada: " + request.portaPon() + ")");
        }

        cto.setIdentificacao(request.identificacao());
        cto.setOlt(olt);
        cto.setPortaPon(request.portaPon());
        cto.setCapacidade(request.capacidade());
        cto.setLatitude(request.latitude());
        cto.setLongitude(request.longitude());
    }
}
