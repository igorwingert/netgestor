package br.edu.utfpr.netgestor.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a segurança de ponta a ponta: login, token JWT, 401, 403 e perfis.
 * Usa os usuários da migration V2. Precisa do PostgreSQL rodando (docker compose up -d).
 * @Transactional desfaz no final o que os testes alterarem no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String login(String email, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }

    @Test
    void loginValidoDevolveTokenEDadosDoUsuario() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@netgestor.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.perfil").value("ADMIN"));
    }

    @Test
    void loginComSenhaErradaDevolve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@netgestor.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    @Test
    void semTokenDevolve401EmJson() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value(containsString("Autenticação necessária")));
    }

    /**
     * Ataque clássico: pegar o token de um técnico e reescrever o payload dizendo que ele é ADMIN.
     * O payload é só Base64 (qualquer um lê e altera), mas a assinatura deixa de conferir -> 401.
     */
    @Test
    void tokenComPayloadAdulteradoDevolve401() throws Exception {
        String[] partes = login("tecnico@netgestor.com", "tecnico123").substring("Bearer ".length()).split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"admin@netgestor.com\",\"perfil\":\"ADMIN\",\"exp\":4102444800}".getBytes(StandardCharsets.UTF_8));
        String tokenFalso = partes[0] + "." + payloadFalso + "." + partes[2];

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenFalso))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Token inválido"));
    }

    @Test
    void meDevolveOUsuarioDoToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", login("tecnico@netgestor.com", "tecnico123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tecnico@netgestor.com"))
                .andExpect(jsonPath("$.perfil").value("TECNICO"));
    }

    @Test
    void atendenteNaoAcessaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", login("atendente@netgestor.com", "atendente123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    /**
     * Corpo válido de propósito: o Spring valida o @RequestBody @Valid ANTES de chamar o método,
     * e o @PreAuthorize atua na chamada do método. Com corpo inválido, a resposta seria 400.
     */
    @Test
    void tecnicoNaoCadastraCliente() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", login("tecnico@netgestor.com", "tecnico123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"cpfCnpj\":\"52998224725\",\"tipoPessoa\":\"FISICA\","
                                + "\"email\":\"teste@email.com\",\"telefone\":\"(41) 90000-0000\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void linksDeAcaoDependemDoPerfil() throws Exception {
        // ATENDENTE vê as ações do contrato ATIVO...
        mockMvc.perform(get("/api/contratos/1").header("Authorization", login("atendente@netgestor.com", "atendente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.suspender").exists())
                .andExpect(jsonPath("$._links.faturas").exists());
        // ...o TECNICO lê o mesmo contrato, mas sem ações nem links que dariam 403
        mockMvc.perform(get("/api/contratos/1").header("Authorization", login("tecnico@netgestor.com", "tecnico123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.suspender").doesNotExist())
                .andExpect(jsonPath("$._links.faturas").doesNotExist())
                .andExpect(jsonPath("$._links.equipamentos").exists());
    }

    @Test
    void tecnicoSoAlteraAsPropriasOrdensDeServico() throws Exception {
        // OS 3 é do Carlos (tecnico@): a Marina (tecnico2@) recebe 403...
        mockMvc.perform(patch("/api/ordens-servico/3/concluir")
                        .header("Authorization", login("tecnico2@netgestor.com", "tecnico123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(containsString("outro técnico")));
        // ...e o próprio Carlos consegue concluir
        mockMvc.perform(patch("/api/ordens-servico/3/concluir")
                        .header("Authorization", login("tecnico@netgestor.com", "tecnico123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void chamadoRegistraQuemAbriuPeloToken() throws Exception {
        mockMvc.perform(post("/api/chamados")
                        .header("Authorization", login("atendente@netgestor.com", "atendente123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contratoId\":1,\"tipo\":\"LENTIDAO\",\"prioridade\":\"MEDIA\",\"descricao\":\"Teste\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.abertoPorNome").value("Juliana Ramos"));
    }
}
