package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

    @Override
    public String createToken(final AuthenticationInfo authenticationInfo) {
        String userId = String.valueOf(authenticationInfo.id());
        String userRole = authenticationInfo.role().name();

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
        AuthenticationInfo authenticationInfo = extractAuthenticationInfo(token);
        return authenticationInfo.id();
    }

    @Override
    public AuthenticationInfo extractAuthenticationInfo(final String token) {
        Claims payload = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long id = Long.parseLong(payload.getSubject());
        UserRole role = UserRole.valueOf(payload.get("role", String.class));

        return new AuthenticationInfo(id, role);
    }

    @Override
    public boolean isValidToken(final String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            boolean isExpired = claims.getPayload().getExpiration().before(new Date());

            return !isExpired;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

