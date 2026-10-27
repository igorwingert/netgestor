package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Plano;
import br.edu.utfpr.netgestor.dto.PlanoRequest;
import br.edu.utfpr.netgestor.dto.PlanoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import br.edu.utfpr.netgestor.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final ContratoRepository contratoRepository;

    @Transactional(readOnly = true)
    public List<PlanoResponse> listar() {
        return planoRepository.findAll(Sort.by("valorMensal")).stream()
                .map(PlanoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanoResponse buscarPorId(Long id) {
        return PlanoResponse.de(buscarEntidade(id));
    }

    public Plano buscarEntidade(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano " + id + " não encontrado"));
    }

    @Transactional
    public PlanoResponse cadastrar(PlanoRequest request) {
        Plano plano = new Plano();
        copiarDados(request, plano);
        return PlanoResponse.de(planoRepository.save(plano));
    }

    @Transactional
    public PlanoResponse atualizar(Long id, PlanoRequest request) {
        Plano plano = buscarEntidade(id);
        copiarDados(request, plano);
        return PlanoResponse.de(plano);
    }

    @Transactional
    public void excluir(Long id) {
        Plano plano = buscarEntidade(id);
        if (contratoRepository.existsByPlanoId(id)) {
            throw new RegraNegocioException("O plano " + plano.getNome()
                    + " possui contratos e não pode ser excluído. Desative-o (ativo = false).");
        }
        planoRepository.delete(plano);
    }

    private void copiarDados(PlanoRequest request, Plano plano) {
        planoRepository.findByNomeIgnoreCase(request.nome())
                .filter(outro -> !outro.getId().equals(plano.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um plano com o nome " + request.nome());
                });

        plano.setNome(request.nome());
        plano.setDownloadMbps(request.downloadMbps());
        plano.setUploadMbps(request.uploadMbps());
        plano.setValorMensal(request.valorMensal());
        plano.setTecnologia(request.tecnologia());
        plano.setAtivo(!Boolean.FALSE.equals(request.ativo()));
    }
}
