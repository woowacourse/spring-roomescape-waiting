package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.global.exception.custom.UnauthorizedException;

@Component
public class JwtTokenProvider {

    private static final String secretKey = "secret";
    private static final long validityInMilliseconds = 300000;

    public String createToken(final String payload) {
        final Claims claims = Jwts.claims().setSubject(payload);
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public long getId(final String token) {
        final String payload = getPayload(token);
        try {
            return Long.parseLong(payload);
        } catch (ArithmeticException e) {
            throw new UnauthorizedException("올바르지 않은 토큰 정보입니다.");
        }
    }

    private String getPayload(final String token) {
        validateToken(token);
        return Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("인증할 수 없는 토큰입니다.");
        }
    }
}
