package br.edu.utfpr.netgestor.dto;

/**
 * resposta do login. o frontend guarda o token e o envia nas proximas requisicoes:
 *   Authorization: Bearer <token>
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario) {
}
