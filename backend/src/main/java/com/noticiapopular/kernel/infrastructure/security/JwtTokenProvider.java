package com.noticiapopular.kernel.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String gerarAccessToken(String usuarioId, String papel) {
        return construirToken(usuarioId, papel, jwtExpirationMs, "access");
    }

    public String gerarRefreshToken(String usuarioId) {
        return construirToken(usuarioId, null, refreshExpirationMs, "refresh");
    }

    private String construirToken(String usuarioId, String papel, long expiracaoMs, String tipo) {
        var agora = new Date();
        var expiracao = new Date(agora.getTime() + expiracaoMs);

        var builder = Jwts.builder()
                .subject(usuarioId)
                .claim("tipo", tipo)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(obterChave());

        if (papel != null) {
            builder.claim("papel", papel);
        }

        return builder.compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(obterChave()).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.warn("JWT inválido (assinatura): {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.debug("JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT não suportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT vazio ou nulo: {}", ex.getMessage());
        }
        return false;
    }

    public String obterUsuarioIdDoToken(String token) {
        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String obterPapelDoToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(obterChave())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("papel", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean ehRefreshToken(String token) {
        try {
            String tipo = Jwts.parser()
                    .verifyWith(obterChave())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("tipo", String.class);
            return "refresh".equals(tipo);
        } catch (Exception ex) {
            return false;
        }
    }

    private SecretKey obterChave() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
