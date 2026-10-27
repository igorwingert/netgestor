package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Olt;
import br.edu.utfpr.netgestor.dto.OltRequest;
import br.edu.utfpr.netgestor.dto.OltResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.CtoRepository;
import br.edu.utfpr.netgestor.repository.OltRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OltService {

    private final OltRepository oltRepository;
    private final CtoRepository ctoRepository;

    @Transactional(readOnly = true)
    public List<OltResponse> listar() {
        return oltRepository.findAll(Sort.by("nome")).stream()
                .map(OltResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public OltResponse buscarPorId(Long id) {
        return OltResponse.de(buscarEntidade(id));
    }

    public Olt buscarEntidade(Long id) {
        return oltRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("OLT " + id + " não encontrada"));
    }

    @Transactional
    public OltResponse cadastrar(OltRequest request) {
        Olt olt = new Olt();
        copiarDados(request, olt);
        return OltResponse.de(oltRepository.save(olt));
    }

    @Transactional
    public OltResponse atualizar(Long id, OltRequest request) {
        Olt olt = buscarEntidade(id);
        // Não deixa reduzir o total de portas abaixo de uma porta PON que já tem CTO ligada
        if (ctoRepository.existsByOltIdAndPortaPonGreaterThan(id, request.totalPortasPon())) {
            throw new RegraNegocioException("Existem CTOs ligadas em portas PON acima de "
                    + request.totalPortasPon() + " nesta OLT");
        }
        copiarDados(request, olt);
        return OltResponse.de(olt);
    }

    @Transactional
    public void excluir(Long id) {
        Olt olt = buscarEntidade(id);
        if (ctoRepository.existsByOltId(id)) {
            throw new RegraNegocioException("A OLT " + olt.getNome() + " possui CTOs e não pode ser excluída");
        }
        oltRepository.delete(olt);
    }

    private void copiarDados(OltRequest request, Olt olt) {
        oltRepository.findByIpGerencia(request.ipGerencia())
                .filter(outra -> !outra.getId().equals(olt.getId()))
                .ifPresent(outra -> {
                    throw new RegraNegocioException("O IP " + request.ipGerencia()
                            + " já está em uso pela OLT " + outra.getNome());
                });

        olt.setNome(request.nome());
        olt.setIpGerencia(request.ipGerencia());
        olt.setFabricante(request.fabricante());
        olt.setLocalizacao(request.localizacao());
        olt.setTotalPortasPon(request.totalPortasPon());
    }
}
