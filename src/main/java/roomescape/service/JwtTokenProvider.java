package roomescape.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import roomescape.model.Member;

@Component
public class JwtTokenProvider implements TokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @Override
    public String createToken(Member member) {
        Map<String, ?> claims = createClaimsByUser(member);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .subject(member.getId().toString())
                .claims(claims)
                .expiration(validity)
                .issuedAt(now)
                .signWith(getSecretKey())
                .compact();
    }

    @Override
    public Claims getPayload(String token) {
        SecretKey key = getSecretKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Map<String, Object> createClaimsByUser(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", member.getRole().toString());
        return claims;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }
}
