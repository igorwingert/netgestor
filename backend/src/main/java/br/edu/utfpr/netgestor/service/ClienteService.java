package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Cliente;
import br.edu.utfpr.netgestor.dto.ClienteRequest;
import br.edu.utfpr.netgestor.dto.ClienteResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ClienteRepository;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio de clientes.
 *
 * Padrão seguido por todos os services do projeto:
 * - recebem/devolvem DTOs; as entidades ficam "dentro" da camada de serviço;
 * - a conversão entidade -> DTO acontece dentro da transação, enquanto ainda é
 *   possível carregar os relacionamentos LAZY;
 * - violações de regra lançam RegraNegocioException (409) e ids inexistentes
 *   lançam RecursoNaoEncontradoException (404).
 */
@Service
@RequiredArgsConstructor // Lombok gera o construtor: o Spring injeta as dependências por ele
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;

    /** Filtros opcionais: nome (parte do nome) e cpfCnpj (parte do documento, com ou sem máscara). */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String nome, String cpfCnpj) {
        // Filtro não informado vira "": na consulta, LIKE '%%' aceita qualquer valor
        String filtroNome = nome == null ? "" : nome.trim();
        String filtroDocumento = cpfCnpj == null ? "" : cpfCnpj.replaceAll("\\D", "");
        return clienteRepository.buscar(filtroNome, filtroDocumento).stream()
                .map(ClienteResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    /** Usado também por outros services que precisam da entidade Cliente. */
    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado"));
    }

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        copiarDados(request, cliente);
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        copiarDados(request, cliente);
        // Não é preciso chamar save(): a entidade foi carregada nesta transação e o JPA
        // grava as alterações automaticamente no commit ("dirty checking").
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        if (contratoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("O cliente " + cliente.getNome()
                    + " possui contratos e não pode ser excluído. Desative-o (ativo = false).");
        }
        // Os endereços são excluídos junto (cascade ALL em Cliente.enderecos)
        clienteRepository.delete(cliente);
    }

    /** Usado no cadastro e na atualização. No cadastro, cliente.getId() ainda é null. */
    private void copiarDados(ClienteRequest request, Cliente cliente) {
        // CPF/CNPJ e e-mail não podem pertencer a OUTRO cliente
        clienteRepository.findByCpfCnpj(request.cpfCnpj())
                .filter(outro -> !outro.getId().equals(cliente.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um cliente com o CPF/CNPJ " + request.cpfCnpj());
                });
        clienteRepository.findByEmail(request.email())
                .filter(outro -> !outro.getId().equals(cliente.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe um cliente com o e-mail " + request.email());
                });

        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTipoPessoa(request.tipoPessoa());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        cliente.setAtivo(!Boolean.FALSE.equals(request.ativo()));
    }
}
