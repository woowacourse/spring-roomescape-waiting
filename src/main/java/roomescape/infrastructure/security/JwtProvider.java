package roomescape.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.application.auth.dto.JwtPayload;
import roomescape.application.support.exception.JwtExtractException;
import roomescape.domain.member.Role;

@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String issue(JwtPayload jwtPayload) {
        Instant currentInstant = clock.instant();
        Instant expireInstant = currentInstant.plus(jwtProperties.getExpireDuration());
        Date currentDate = Date.from(currentInstant);
        Date expireDate = Date.from(expireInstant);
        return Jwts.builder()
                .issuedAt(currentDate)
                .expiration(expireDate)
                .claims()
                .add("memberId", jwtPayload.memberId())
                .add("name", jwtPayload.name())
                .add("role", jwtPayload.role())
                .and()
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }

    public JwtPayload extractPayload(String token) {
        Claims claims = extractClaims(token);
        return new JwtPayload(
                claims.get("memberId", Long.class),
                claims.get("name", String.class),
                Role.valueOf(claims.get("role", String.class))
        );
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtProperties.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtExtractException("만료된 토큰입니다.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtExtractException("잘못된 형식의 토큰입니다.", e);
        }
    }
}
