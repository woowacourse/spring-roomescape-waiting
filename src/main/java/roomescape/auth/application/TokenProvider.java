package roomescape.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.exception.InvalidTokenException;
import roomescape.auth.exception.TokenIsEmptyException;
import roomescape.member.domain.Member;

@Component
public class TokenProvider {

    public static final String ROLE_CLAIM_NAME = "role";
    private final String secretKey;
    private final long validityInMilliseconds;

    public TokenProvider(@Value("${jwt.secretKey}") String secretKey,
                         @Value("${jwt.expiration}") long validityInMilliseconds) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(member.getId().toString())
                .claim(ROLE_CLAIM_NAME, member.getRole().getName())
                .expiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public Long getMemberId(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }


    public String getRoleName(String token) {
        Claims claims = getClaims(token);
        return claims.get(ROLE_CLAIM_NAME, String.class);
    }

    private Claims getClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenIsEmptyException();
        }

        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

}
