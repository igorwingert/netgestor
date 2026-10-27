package br.edu.utfpr.netgestor.service;

import br.edu.utfpr.netgestor.domain.Cliente;
import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.Cto;
import br.edu.utfpr.netgestor.domain.Endereco;
import br.edu.utfpr.netgestor.domain.Plano;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.domain.enums.Tecnologia;
import br.edu.utfpr.netgestor.dto.ContratoRequest;
import br.edu.utfpr.netgestor.dto.ContratoResponse;
import br.edu.utfpr.netgestor.exception.RecursoNaoEncontradoException;
import br.edu.utfpr.netgestor.exception.RegraNegocioException;
import br.edu.utfpr.netgestor.repository.ChamadoRepository;
import br.edu.utfpr.netgestor.repository.ContratoRepository;
import br.edu.utfpr.netgestor.repository.EquipamentoRepository;
import br.edu.utfpr.netgestor.repository.FaturaRepository;
import br.edu.utfpr.netgestor.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final ChamadoRepository chamadoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final FaturaRepository faturaRepository;
    private final ClienteService clienteService;
    private final PlanoService planoService;
    private final EnderecoService enderecoService;
    private final CtoService ctoService;

    @Transactional(readOnly = true)
    public List<ContratoResponse> listar() {
        return contratoRepository.findAll(Sort.by("numero")).stream()
                .map(ContratoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscarPorId(Long id) {
        return ContratoResponse.de(buscarEntidade(id));
    }

    public Contrato buscarEntidade(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato " + id + " não encontrado"));
    }

    @Transactional
    public ContratoResponse cadastrar(ContratoRequest request) {
        Contrato contrato = new Contrato();
        contrato.setNumero(gerarNumero());
        copiarDados(request, contrato);
        return ContratoResponse.de(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponse atualizar(Long id, ContratoRequest request) {
        Contrato contrato = buscarEntidade(id);
        if (contrato.getStatus() == StatusContrato.CANCELADO) {
            throw new RegraNegocioException("O contrato " + contrato.getNumero()
                    + " está cancelado e não pode ser alterado");
        }
        copiarDados(request, contrato);
        return ContratoResponse.de(contrato);
    }

    @Transactional
    public void excluir(Long id) {
        Contrato contrato = buscarEntidade(id);
        boolean temDependentes = faturaRepository.existsByContratoId(id)
                || chamadoRepository.existsByContratoId(id)
                || ordemServicoRepository.existsByContratoId(id)
                || equipamentoRepository.existsByContratoId(id);
        if (temDependentes) {
            throw new RegraNegocioException("O contrato " + contrato.getNumero()
                    + " possui faturas, chamados, ordens de serviço ou equipamentos e não pode ser excluído."
                    + " Use a ação de cancelar.");
        }
        contratoRepository.delete(contrato);
    }

    private void copiarDados(ContratoRequest request, Contrato contrato) {
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        Plano plano = planoService.buscarEntidade(request.planoId());
        Endereco endereco = enderecoService.buscarEntidade(request.enderecoInstalacaoId());
        Cto cto = request.ctoId() == null ? null : ctoService.buscarEntidade(request.ctoId());

        // Plano inativo não aceita novos contratos (mas contratos antigos podem continuar nele)
        boolean trocouDePlano = contrato.getPlano() == null || !contrato.getPlano().getId().equals(plano.getId());
        if (!plano.isAtivo() && trocouDePlano) {
            throw new RegraNegocioException("O plano " + plano.getNome() + " está inativo e não aceita novos contratos");
        }
        if (!endereco.getCliente().getId().equals(cliente.getId())) {
            throw new RegraNegocioException("O endereço de instalação informado não pertence ao cliente "
                    + cliente.getNome());
        }
        validarPortaCto(contrato, plano, cto, request.portaCto());

        contrato.setCliente(cliente);
        contrato.setPlano(plano);
        contrato.setEnderecoInstalacao(endereco);
        contrato.setCto(cto);
        contrato.setPortaCto(request.portaCto());
        contrato.setDataInicio(request.dataInicio());
        contrato.setDiaVencimento(request.diaVencimento());
    }

    /**
     * Regras da rede óptica:
     * - plano via rádio não usa CTO; plano de fibra exige CTO e porta;
     * - a porta precisa existir na CTO (até a capacidade);
     * - a porta não pode estar ocupada por outro contrato não cancelado.
     */
    private void validarPortaCto(Contrato contrato, Plano plano, Cto cto, Integer porta) {
        if (plano.getTecnologia() == Tecnologia.RADIO) {
            if (cto != null || porta != null) {
                throw new RegraNegocioException("Planos via rádio não usam CTO: não informe ctoId nem portaCto");
            }
            return;
        }
        if (cto == null || porta == null) {
            throw new RegraNegocioException("Planos de fibra exigem ctoId e portaCto");
        }
        if (porta > cto.getCapacidade()) {
            throw new RegraNegocioException("A CTO " + cto.getIdentificacao() + " tem apenas "
                    + cto.getCapacidade() + " portas (porta informada: " + porta + ")");
        }
        contratoRepository.findByCtoIdAndPortaCtoAndStatusNot(cto.getId(), porta, StatusContrato.CANCELADO)
                .filter(outro -> !outro.getId().equals(contrato.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("A porta " + porta + " da CTO " + cto.getIdentificacao()
                            + " já está ocupada pelo contrato " + outro.getNumero());
                });
    }

    /** Gera o próximo número do ano: CT-2026-0001, CT-2026-0002... */
    private String gerarNumero() {
        String prefixo = "CT-" + Year.now() + "-";
        long sequencia = contratoRepository.countByNumeroStartingWith(prefixo) + 1;
        return prefixo + String.format("%04d", sequencia);
    }
}
