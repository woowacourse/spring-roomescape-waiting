package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;

@Component
public class JwtTokenHandler implements AuthenticationTokenHandler {

    private static final SecretKey SECRET_KEY = SIG.HS256.key().build();
    private static final long EXPIRATION_DURATION_IN_MILLISECONDS = 900_000L;

    public String createToken(final AuthenticationInfo authenticationInfo) {
        var userId = String.valueOf(authenticationInfo.id());
        var userRole = authenticationInfo.role().name();
        Claims claims = Jwts.claims()
                .subject(userId)
                .add("role", userRole)
                .build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + EXPIRATION_DURATION_IN_MILLISECONDS);

        return Jwts.builder()
                .claims(claims)
                .expiration(validity)
                .signWith(SECRET_KEY)
                .compact();
    }

    @Override
    public long extractId(final String token) {
        var authenticationInfo = extractAuthenticationInfo(token);
        return authenticationInfo.id();
    }

    public AuthenticationInfo extractAuthenticationInfo(final String token) {
        var payload = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        var id = Long.parseLong(payload.getSubject());
        var role = UserRole.valueOf(payload.get("role", String.class));
        return new AuthenticationInfo(id, role);
    }

    public boolean isValidToken(final String token) {
        try {
            var claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            var isExpired = claims.getPayload().getExpiration().before(new Date());
            return !isExpired;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

