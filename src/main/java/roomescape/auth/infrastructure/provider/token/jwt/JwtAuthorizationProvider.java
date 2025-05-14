package roomescape.auth.infrastructure.provider.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.provider.token.TokenAuthorizationProvider;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.MemberRole;

@Component
public class JwtAuthorizationProvider extends TokenAuthorizationProvider {
    private final String secretKey;
    private final long validityInMilliseconds;

    public JwtAuthorizationProvider(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.validity-in-milliseconds}") long validityInMilliseconds
    ) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    @Override
    public String createToken(AuthorizationPayload payload) {
        Claims claims = Jwts.claims();
        claims.put("name", payload.name());
        claims.put("role", payload.role());

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    @Override
    public AuthorizationPayload getPayload(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();

        String name = claims.get("name", String.class);
        MemberRole role = MemberRole.valueOf(claims.get("role", String.class));

        return new AuthorizationPayload(name, role);
    }

    @Override
    public void validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            claims.getBody().getExpiration();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("인증 정보가 올바르지 않습니다.");
        }
    }
}
