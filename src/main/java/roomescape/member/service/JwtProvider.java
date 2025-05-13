package roomescape.member.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenProvider {
    private static final SignatureAlgorithm SIGN_ALGORITHM = SignatureAlgorithm.HS256;

    @Value("${security.jwt.token.secret_key}")
    private String SECRET_KEY;
    @Value("${security.jwt.token.expiration_term}")
    private long EXPIRATION_TERM;

    @Override
    public String createToken(final String payload) {
        final Date now = new Date();
        final Date expirationDate = new Date(now.getTime() + EXPIRATION_TERM);
        return Jwts.builder()
                .setSubject(payload)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SIGN_ALGORITHM, SECRET_KEY)
                .compact();
    }

    @Override
    public String parsePayload(final String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }
}
