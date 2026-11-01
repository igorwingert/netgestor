package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Fatura;
import br.edu.utfpr.netgestor.domain.enums.Prioridade;
import br.edu.utfpr.netgestor.domain.enums.StatusChamado;
import br.edu.utfpr.netgestor.domain.enums.StatusContrato;
import br.edu.utfpr.netgestor.domain.enums.StatusFatura;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * testa as consultas com filtros opcionais (@Query JPQL) sobre os dados da V2,
 * tanto com filtro preenchido quanto com filtro nulo ("sem filtro").
 * precisa do PostgreSQL rodando (docker compose up -d).
 */
@SpringBootTest
@Transactional
class ConsultasJpaTest {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ContratoRepository contratoRepository;
    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private FaturaRepository faturaRepository;

    @Test
    void clientesPorParteDoNomeOuDocumento() {
        assertThat(clienteRepository.buscar("silva", "")).hasSize(1);
        assertThat(clienteRepository.buscar("", "11222333")).hasSize(1);
        assertThat(clienteRepository.buscar("", "")).hasSize(7); // 6 da V2 + 1 da V3
        assertThat(clienteRepository.buscar("", "ABC345")).hasSize(1); // CNPJ alfanumerico (V3)
    }

    @Test
    void contratosPorStatusECliente() {
        assertThat(contratoRepository.buscar(StatusContrato.ATIVO, null)).hasSize(4);
        assertThat(contratoRepository.buscar(null, 2L)).hasSize(2);
        assertThat(contratoRepository.buscar(StatusContrato.CANCELADO, 2L)).hasSize(1);
        assertThat(contratoRepository.buscar(null, null)).hasSize(6);
    }

    @Test
    void chamadosPorStatusEPrioridade() {
        assertThat(chamadoRepository.buscar(StatusChamado.ABERTO, Prioridade.ALTA, null)).hasSize(1);
        assertThat(chamadoRepository.buscar(StatusChamado.ABERTO, null, null)).hasSize(2);
    }

    /** este caso quebrava sem o CAST no filtro de data (erro "could not determine data type"). */
    @Test
    void faturasPorStatusECompetencia() {
        LocalDate outubro = LocalDate.of(2026, 10, 1);
        assertThat(faturaRepository.buscar(StatusFatura.VENCIDA, outubro, null))
                .extracting(Fatura::getId)
                .containsExactlyInAnyOrder(7L, 14L);
        assertThat(faturaRepository.buscar(null, outubro, null)).hasSize(5);
        assertThat(faturaRepository.buscar(null, null, 1L)).hasSize(4);
        assertThat(faturaRepository.buscar(null, null, null)).hasSize(17);
    }
}
