package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.Role;
import roomescape.exception.UnauthenticatedException;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";

    private final Key secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.token.secret-key}")
            String rawSecretKey,

            @Value("${security.jwt.token.expire-length}")
            long validityInMilliseconds
    ) {
        final byte[] keyBytes = Base64.getDecoder().decode(rawSecretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(Long userId, String username, Role role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role.name())
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parse(token).getSubject());
    }

    public String getUsername(String token) {
        return parse(token).get(CLAIM_USERNAME, String.class);
    }

    public Role getRole(String token) {
        return Role.valueOf(parse(token).get(CLAIM_ROLE, String.class));
    }

    private Claims parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthenticatedException();
        }
    }
}
