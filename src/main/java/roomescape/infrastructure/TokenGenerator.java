package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    private final JwtTokenProperties jwtTokenProperties;

    public TokenGenerator(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    public String createToken(String payload, String role) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtTokenProperties.getExpireLength());

        return Jwts.builder()
                .setClaims(claims)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, jwtTokenProperties.getSecretKey())
                .compact();
    }

    public String getPayload(String token) {
        return Jwts.parser()
                .setSigningKey(jwtTokenProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
