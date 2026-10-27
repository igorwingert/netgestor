package br.edu.utfpr.netgestor.dto;

/**
 * Resposta do login. O frontend guarda o token e o envia nas próximas requisições:
 *   Authorization: Bearer <token>
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario) {
}
