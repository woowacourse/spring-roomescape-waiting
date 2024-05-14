package roomescape.service.fake;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import roomescape.model.Member;
import roomescape.service.TokenProvider;

public class FakeJwtTokenProvider implements TokenProvider {

    private String secretKey = "eyJhbGciOiJIUzI1NiIsInR5WIiOiIiLCJuYW1lIjoiSm9obiBEb24k1fagApg3qLWiB8Kt59Lno";

    @Override
    public String createToken(Member member) {
        Map<String, ?> claims = createClaimsByUser(member);
        return Jwts.builder()
                .subject(member.getId().toString())
                .claims(claims)
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
