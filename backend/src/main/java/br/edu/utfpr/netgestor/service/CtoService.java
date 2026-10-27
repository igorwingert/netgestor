package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Cto;
import br.edu.utfpr.netgestor.domain.Olt;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.dto.CtoRequest;
import br.edu.utfpr.netgestor.dto.CtoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import br.edu.utfpr.netgestor.repository.CtoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
