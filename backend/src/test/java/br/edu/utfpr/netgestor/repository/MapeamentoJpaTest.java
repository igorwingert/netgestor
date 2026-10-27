package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Cliente;
import br.edu.utfpr.netgestor.domain.Contrato;
import br.edu.utfpr.netgestor.domain.OrdemServico;
import br.edu.utfpr.netgestor.domain.Tecnico;
import br.edu.utfpr.netgestor.domain.Usuario;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lê os dados inseridos pela migration V2 através das entidades JPA,
 * conferindo que cada tipo de relacionamento está mapeado corretamente.
 * Precisa do PostgreSQL rodando (docker compose up -d).
 *
 * @Transactional mantém a sessão aberta durante o teste (para navegar nos
 * relacionamentos LAZY) e desfaz qualquer alteração no final.
 */
@SpringBootTest
@Transactional
class MapeamentoJpaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ContratoRepository contratoRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;
    @Autowired
    private OrdemServicoRepository ordemServicoRepository;
    @Autowired
    private FaturaRepository faturaRepository;

    @Test
    void senhasIniciaisConferemComOsHashesBCrypt() {
        var encoder = new BCryptPasswordEncoder();
        assertThat(usuarioRepository.findAll())
                .extracting(Usuario::getEmail)
                .contains("admin@netgestor.com", "atendente@netgestor.com", "tecnico@netgestor.com");

        Usuario admin = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("admin@netgestor.com")).findFirst().orElseThrow();
        assertThat(encoder.matches("admin123", admin.getSenha())).isTrue();
    }

    @Test
    void oneToManyClienteComEnderecos() {
        Cliente joao = clienteRepository.findById(1L).orElseThrow();
        assertThat(joao.getEnderecos()).hasSize(2);
    }

    @Test
    void manyToOneContratoComClientePlanoECto() {
        Contrato contrato = contratoRepository.findById(1L).orElseThrow();
        assertThat(contrato.getCliente().getNome()).isEqualTo("João da Silva");
        assertThat(contrato.getPlano().getNome()).isEqualTo("Fibra 500 Mega");
        assertThat(contrato.getCto().getOlt().getNome()).isEqualTo("OLT-CENTRO");
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    @Test
    void oneToOneTecnicoComUsuario() {
        Tecnico carlos = tecnicoRepository.findById(1L).orElseThrow();
        assertThat(carlos.getUsuario().getEmail()).isEqualTo("tecnico@netgestor.com");
    }

    @Test
    void manyToManyOrdemServicoComEquipamentos() {
        OrdemServico os = ordemServicoRepository.findById(1L).orElseThrow();
        assertThat(os.getEquipamentos())
                .extracting(e -> e.getNumeroSerie())
                .containsExactly("HWTC1A2B3C01");
    }

    @Test
    void oneToManyFaturaComPagamentos() {
        assertThat(faturaRepository.findById(13L).orElseThrow().getPagamentos()).hasSize(1);
    }
}
