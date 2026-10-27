package br.edu.utfpr.netgestor.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gera e valida os tokens JWT.
 *
 * Um JWT tem três partes separadas por ponto: header.payload.assinatura
 *   - header:     algoritmo usado (HMAC-SHA)
 *   - payload:    as "claims", ex.: {"sub":"admin@netgestor.com","perfil":"ADMIN","exp":...}
 *   - assinatura: calculada com a chave secreta do servidor
 * O payload é apenas codificado (Base64), não criptografado: qualquer um consegue ler,
 * mas ninguém consegue ALTERAR sem invalidar a assinatura. Por isso não há senha no token.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    /** Os valores vêm do application.yml (variáveis de ambiente JWT_SECRET e JWT_EXPIRATION). */
    public JwtService(@Value("${netgestor.jwt.secret}") String segredo,
                      @Value("${netgestor.jwt.expiration}") long expiracaoMs) {
        // HMAC exige chave de pelo menos 256 bits (32 caracteres)
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(UsuarioLogado usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())              // "dono" do token
                .claim("id", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    /**
     * Confere a assinatura e a validade e devolve o e-mail (subject) do token.
     * Lança ExpiredJwtException se o token venceu, ou outra JwtException se for inválido/adulterado.
     */
    public String extrairEmail(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getExpiracaoSegundos() {
        return expiracaoMs / 1000;
    }
}
