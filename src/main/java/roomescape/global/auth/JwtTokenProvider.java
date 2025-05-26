package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Role;

@Component
public class JwtTokenProvider {

    private static final String secretKey = "secret";
    private static final long validityInMilliseconds = 300000;
    private static final String ROLE_CLAIM = "role";
    private static final String NAME_CLAIM = "name";

    public String createToken(final Long id, final Role role, final String name) {
        final Claims claims = Jwts.claims().setSubject(id.toString());
        claims.put(ROLE_CLAIM, role.name());
        claims.put(NAME_CLAIM, name);
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
        final Claims claims = getClaims(token);
        try {
            return Long.parseLong(claims.getSubject());
        } catch (ArithmeticException e) {
            throw new UnauthorizedException("올바르지 않은 토큰 정보입니다.");
        }
    }

    public Role getRole(final String token) {
        final Claims claims = getClaims(token);
        return Role.valueOf(claims.get(ROLE_CLAIM, String.class));
    }

    public String getName(final String token) {
        final Claims claims = getClaims(token);
        return claims.get(NAME_CLAIM, String.class);
    }

    private Claims getClaims(final String token) {
        validateToken(token);
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("인증할 수 없는 토큰입니다.");
        }
    }
}
