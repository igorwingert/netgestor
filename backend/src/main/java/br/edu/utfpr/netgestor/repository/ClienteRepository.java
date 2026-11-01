package br.edu.utfpr.netgestor.repository;

import br.edu.utfpr.netgestor.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    Optional<Cliente> findByEmail(String email);

    /**
     * GET /api/clientes?nome=&cpfCnpj=
     * busca parcial e sem diferenciar maiusculas. o service troca filtro nulo por "",
     * e LIKE '%%' aceita qualquer valor (ou seja, "sem filtro").
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
              AND c.cpfCnpj LIKE CONCAT('%', :cpfCnpj, '%')
            ORDER BY c.nome
            """)
    List<Cliente> buscar(@Param("nome") String nome, @Param("cpfCnpj") String cpfCnpj);
}
