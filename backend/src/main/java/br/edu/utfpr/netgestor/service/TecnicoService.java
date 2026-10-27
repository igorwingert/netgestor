package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Tecnico;
import br.edu.utfpr.netgestor.domain.Usuario;
import br.edu.utfpr.netgestor.domain.enums.Perfil;
import br.edu.utfpr.netgestor.dto.TecnicoRequest;
import br.edu.utfpr.netgestor.dto.TecnicoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import br.edu.utfpr.netgestor.repository.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return tecnicoRepository.findAll(Sort.by("nome")).stream()
                .map(TecnicoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscarPorId(Long id) {
        return TecnicoResponse.de(buscarEntidade(id));
    }

    public Tecnico buscarEntidade(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico " + id + " não encontrado"));
    }

    @Transactional
    public TecnicoResponse cadastrar(TecnicoRequest request) {
        Tecnico tecnico = new Tecnico();
        copiarDados(request, tecnico);
        return TecnicoResponse.de(tecnicoRepository.save(tecnico));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscarEntidade(id);
        copiarDados(request, tecnico);
        return TecnicoResponse.de(tecnico);
    }

    @Transactional
    public void excluir(Long id) {
        Tecnico tecnico = buscarEntidade(id);
        if (ordemServicoRepository.existsByTecnicoId(id)) {
            throw new RegraNegocioException("O técnico " + tecnico.getNome()
                    + " possui ordens de serviço e não pode ser excluído");
        }
        tecnicoRepository.delete(tecnico);
    }

    private void copiarDados(TecnicoRequest request, Tecnico tecnico) {
        Usuario usuario = usuarioService.buscarEntidade(request.usuarioId());
        if (usuario.getPerfil() != Perfil.TECNICO) {
            throw new RegraNegocioException("O usuário " + usuario.getEmail() + " não tem o perfil TECNICO");
        }
        // Relacionamento 1:1: o usuário não pode estar ligado a OUTRO técnico
        tecnicoRepository.findByUsuarioId(usuario.getId())
                .filter(outro -> !outro.getId().equals(tecnico.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("O usuário " + usuario.getEmail()
                            + " já está vinculado ao técnico " + outro.getNome());
                });

        tecnico.setNome(request.nome());
        tecnico.setTelefone(request.telefone());
        tecnico.setEspecialidade(request.especialidade());
        tecnico.setUsuario(usuario);
    }
}
