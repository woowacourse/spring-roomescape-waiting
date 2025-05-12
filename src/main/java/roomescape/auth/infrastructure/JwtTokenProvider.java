package roomescape.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.exception.InvalidTokenException;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${auth.jwt.secret-key}")
    private String secretKey;

    @Value("${auth.jwt.validity-ms}")
    private long validityInMilliseconds;

    public String createToken(String payload) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getPayload(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException expiredJwtException) {
            throw new InvalidTokenException("만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException jwtException) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }
}
