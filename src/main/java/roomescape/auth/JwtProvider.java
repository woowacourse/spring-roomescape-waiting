package roomescape.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.member.MemberRole;

@Component
public class JwtProvider {

    private static final String SECRET_KEY = "regjeoigjroigji3j2io3io4h2bjasbdjaksbdkjqu3hu23hru3rhashudhausdhas";
    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    private static final Long VALIDITY_IN_MILLISECONDS = 3600_000L;

    public String provideToken(final String email, final MemberRole role, final String name) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

        return Jwts.builder()
                .claim("role", role.name())
                .claim("name", name)

                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)

                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public TokenBody extractBody(final String token) {
        return new TokenBody(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    public boolean isValidToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token).getBody();
            return true;
        } catch (final JwtException e) {
            return false;
        }
    }
}
