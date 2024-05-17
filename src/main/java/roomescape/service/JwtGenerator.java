package roomescape.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtGenerator {

    private final long expireMillisecond;
    private final String secretKey;

    public JwtGenerator(
            @Value("${security.jwt.token.secret-key}") String secretKey,
            @Value("${security.jwt.token.expire-length}") long expireMillisecond) {
        this.secretKey = secretKey;
        this.expireMillisecond = expireMillisecond;
    }

    public String generateWith(Map<String, Object> claimDatas) {
        Date now = new Date();
        return Jwts.builder()
                .addClaims(Jwts.claims(claimDatas))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims getClaims(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
