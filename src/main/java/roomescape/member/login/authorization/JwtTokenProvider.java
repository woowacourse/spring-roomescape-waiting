package roomescape.member.login.authorization;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String CLAIM_NAME = "role";

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String createToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_NAME, role)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public String getPayloadEmail(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getPayloadRole(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(CLAIM_NAME, String.class);
    }
}
