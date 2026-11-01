package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Usuario;
import br.edu.utfpr.netgestor.dto.UsuarioRequest;
import br.edu.utfpr.netgestor.dto.UsuarioResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ChamadoRepository;
import br.edu.utfpr.netgestor.repository.TecnicoRepository;
import br.edu.utfpr.netgestor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ChamadoRepository chamadoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("nome")).stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(buscarEntidade(id));
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        copiarDados(request, usuario);
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);
        copiarDados(request, usuario);
        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarEntidade(id);
        if (tecnicoRepository.existsByUsuarioId(id)) {
            throw new RegraNegocioException("O usuário " + usuario.getEmail()
                    + " está vinculado a um técnico e não pode ser excluído. Desative-o (ativo = false).");
        }
        if (chamadoRepository.existsByAbertoPorId(id)) {
            throw new RegraNegocioException("O usuário " + usuario.getEmail()
                    + " abriu chamados e não pode ser excluído. Desative-o (ativo = false).");
        }
        usuarioRepository.delete(usuario);
    }

    private void copiarDados(UsuarioRequest request, Usuario usuario) {
        usuarioRepository.findByEmail(request.email())
                .filter(outro -> !outro.getId().equals(usuario.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um usuário com o e-mail " + request.email());
                });

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        // a senha nunca e gravada pura: guardamos apenas o hash BCrypt
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(!Boolean.FALSE.equals(request.ativo()));
    }
}
