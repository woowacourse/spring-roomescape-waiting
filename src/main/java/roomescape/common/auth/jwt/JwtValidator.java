package roomescape.common.auth.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtValidator {

    private final Key key;

    @Value("${roomescape.auth.jwt.access_expiration}")
    private Long tokenExpiration;

    public JwtValidator(@Value("${roomescape.auth.jwt.key}") String key) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
    }

    public boolean validateJwtToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();
            parser.parseClaimsJws(token).getBody();
            return true;
        }catch (JwtException e){
            return false;
        }
    }

}
