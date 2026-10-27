package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Cliente;
import br.edu.utfpr.netgestor.domain.Endereco;
import br.edu.utfpr.netgestor.dto.EnderecoRequest;
import br.edu.utfpr.netgestor.dto.EnderecoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import br.edu.utfpr.netgestor.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Endereços são um sub-recurso do cliente: /api/clientes/{clienteId}/enderecos. */
@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final ContratoRepository contratoRepository;
    private final ClienteService clienteService;

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listarPorCliente(Long clienteId) {
        clienteService.buscarEntidade(clienteId); // 404 se o cliente não existir
        return enderecoRepository.findByClienteIdOrderById(clienteId).stream()
                .map(EnderecoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnderecoResponse buscarPorId(Long id) {
        return EnderecoResponse.de(buscarEntidade(id));
    }

    public Endereco buscarEntidade(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço " + id + " não encontrado"));
    }

    @Transactional
    public EnderecoResponse cadastrar(Long clienteId, EnderecoRequest request) {
        Cliente cliente = clienteService.buscarEntidade(clienteId);
        Endereco endereco = new Endereco();
        copiarDados(request, endereco);
        cliente.adicionarEndereco(endereco);
        return EnderecoResponse.de(enderecoRepository.save(endereco));
    }

    @Transactional
    public EnderecoResponse atualizar(Long id, EnderecoRequest request) {
        Endereco endereco = buscarEntidade(id);
        copiarDados(request, endereco);
        return EnderecoResponse.de(endereco);
    }

    @Transactional
    public void excluir(Long id) {
        Endereco endereco = buscarEntidade(id);
        if (contratoRepository.existsByEnderecoInstalacaoId(id)) {
            throw new RegraNegocioException(
                    "O endereço é usado como endereço de instalação de um contrato e não pode ser excluído");
        }
        // orphanRemoval: ao tirar o endereço da lista do cliente, o JPA executa o DELETE
        endereco.getCliente().getEnderecos().remove(endereco);
    }

    private void copiarDados(EnderecoRequest request, Endereco endereco) {
        endereco.setCep(request.cep());
        endereco.setLogradouro(request.logradouro());
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro());
        endereco.setCidade(request.cidade());
        endereco.setUf(request.uf());
    }
}
