package roomescape.common.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtExtractor {

    private final Key key;

    @Value("${roomescape.auth.jwt.access_expiration}")
    private Long tokenExpiration;

    public JwtExtractor(@Value("${roomescape.auth.jwt.key}") String key) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
    }

    public Optional<String> extractJwtToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(refreshToken -> refreshToken.startsWith("Bearer "))
                .map(refreshToken -> refreshToken.replace("Bearer ", ""));
    }

    public Long getId(String token){
        String id = getClaimFromToken(token, "id");
        return Long.parseLong(id);
    }

    public String getName(String token){
        return getClaimFromToken(token, "name");
    }

    public String getRole(String token) {
        return getClaimFromToken(token, "role");
    }

    public Boolean isExpired(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().before(new Date());
    }

    private String getClaimFromToken(String token, String claimName) {
        Claims claims = parseClaims(token);
        return claims.get(claimName, String.class);
    }

    private Claims parseClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims;
    }

}
