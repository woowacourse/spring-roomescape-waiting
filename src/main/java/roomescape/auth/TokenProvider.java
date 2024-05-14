package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.exception.UnauthorizedException;

import java.util.Date;

@Component
public class TokenProvider {
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validTime = 3600000;

    public String create(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .claim("id", member.getId())
                .claim("role", member.getRole())
                .claim("email", member.getEmail())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public String extractMemberEmail(String token) {
        Claims claims = getPayload(token);
        return (String) claims.get("email");
    }

    public long extractMemberId(String token) {
        Claims claims = getPayload(token);
        return claims.get("id", Long.class);
    }

    public String extractMemberRole(String token) {
        Claims claims = getPayload(token);
        return (String) claims.get("role");
    }

    public Claims getPayload(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            validateExpiration(claims);
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("권한이 없는 접근입니다.");
        }
    }

    public void validateExpiration(Claims claims) {
        if (claims.getExpiration().before(new Date())) {
            throw new UnauthorizedException("권한이 없는 접근입니다.");
        }
    }
}
