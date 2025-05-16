package roomescape.provider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.entity.Member;
import roomescape.exception.custom.AuthenticatedException;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public String createToken(Member member) {
        return Jwts.builder()
            .setSubject(member.getId().toString())
            .claim("email", member.getEmail())
            .claim("name", member.getName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();
    }

    public Long getMemberIdFromToken(String token) {
        try {
            return Long.valueOf(Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
        } catch (ExpiredJwtException e) {
            throw new AuthenticatedException("토큰 만료");
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticatedException("유효하지 않은 토큰");
        }
    }
}
