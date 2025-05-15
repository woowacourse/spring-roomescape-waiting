package roomescape.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import roomescape.exception.TokenCreationException;
import roomescape.exception.UnauthorizedException;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final JwtParser jwtParser;
    private final Duration validity;

    public JwtTokenProvider(final JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
        this.jwtParser = Jwts.parser()
                .verifyWith(key)
                .build();
        this.validity = Duration.ofMillis(jwtProperties.getExpireLength());
    }

    @Override
    public String createToken(final Claims claims) {
        final Instant now = Instant.now();
        try {
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(validity)))
                    .signWith(key)
                    .compact();
        } catch (IllegalArgumentException | JwtException e) {
            throw new TokenCreationException("토큰 생성 중 오류가 발생했습니다.");
        }
    }

    @Override
    public String extractPrincipal(final String token) {
        return parseAllClaims(token).getSubject();
    }

    private Claims parseAllClaims(final String token) {
        try {
            final Jwt<?, Claims> jwt = jwtParser.parseSignedClaims(token);
            return jwt.getPayload();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new UnauthorizedException("유효하지 않은 토큰 형식입니다.");
        } catch (SignatureException e) {
            throw new UnauthorizedException("토큰 서명이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("토큰이 제공되지 않았거나 잘못된 값입니다.");
        }
    }
}
