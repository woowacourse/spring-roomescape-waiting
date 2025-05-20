package roomescape.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final int jwtExpirationMs;
    private final SecretKey key;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String jwtSecret,
                            @Value("${security.jwt.token.expire-length}") int jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Member member) {
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("name", member.getName())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public Long getMemberIdFromToken(String token) {
        return Long.valueOf(getJwtClaims(token).getSubject());
    }

    private Claims getJwtClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            throw new UnauthorizedException(ExceptionCause.JWT_TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ExceptionCause.JWT_TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(ExceptionCause.JWT_TOKEN_EMPTY);
        }
    }
}
