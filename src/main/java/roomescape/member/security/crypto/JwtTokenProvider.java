package roomescape.member.security.crypto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;

@Component
public class JwtTokenProvider implements TokenProvider {
    private final String secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.token.secret-key}") String secretKey,
            @Value("${security.jwt.token.expire-length}") long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    @Override
    public String createToken(Member member, Date issuedAt) {
        Claims claims = Jwts.claims()
                .setSubject(String.valueOf(member.getId()));
        claims.put("email", member.getEmail());
        claims.put("name", member.getName());

        Date validity = new Date(issuedAt.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    @Override
    public Map<String, String> getPayload(String token) {
        Jws<Claims> claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);

        Claims claimsBody = claims.getBody();
        String id = claimsBody.getSubject();
        String email = claimsBody.get("email", String.class);
        String name = claimsBody.get("name", String.class);

        return Map.of(
                "id", id,
                "email", email,
                "name", name
        );
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);

            return claims.getBody()
                    .getExpiration()
                    .after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
