package br.com.monitoramento.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gera e valida tokens JWT assinados com HMAC-SHA256.
 *
 * <p>O segredo de assinatura vem de {@code app.jwt.secret} (variável de ambiente
 * {@code JWT_SECRET}) e deve ter no mínimo 32 caracteres para atender ao requisito
 * de chave de 256 bits do algoritmo HS256.</p>
 */
@Component
public class JwtService {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(String username, String role) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(signingKey)
                .compact();
    }

    public String extrairUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extrairRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /**
     * Valida assinatura, expiração e correspondência com o username esperado.
     */
    public boolean isTokenValido(String token, String usernameEsperado) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(usernameEsperado) && !isExpirado(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isExpirado(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Retorna os claims mesmo expirados para permitir mensagens de erro específicas;
            // isTokenValido() ainda rejeitará o token por expiração.
            return e.getClaims();
        }
    }
}
