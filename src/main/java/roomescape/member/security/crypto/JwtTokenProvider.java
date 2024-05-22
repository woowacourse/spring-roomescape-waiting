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
                .setSubject(member.getEmail());
        claims.put("name", member.getName());
        claims.put("id", String.valueOf(member.getId()));

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
        String email = claimsBody.getSubject();
        String name = claimsBody.get("name", String.class);
        String id = claimsBody.get("id", String.class);

        return Map.of(
                "email", email,
                "name", name,
                "id", id
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
