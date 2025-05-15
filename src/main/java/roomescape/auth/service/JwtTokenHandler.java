package roomescape.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.common.exception.AuthenticationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.util.Date;

@Component
public class JwtTokenHandler {

    private static final String CLAIM_ROLE = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    private JwtParser parser;

    @PostConstruct
    public void init() {
        this.parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();
    }

    public String createToken(Member member) {
        Claims claims = Jwts.claims()
                .subject(member.getId().toString())
                .add(CLAIM_ROLE, member.getRole().name())
                .build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public Long getSubject(String token) {
        try {
            Claims claims = getBodyWithValidation(token);
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 형식의 토큰입니다.");
        }
    }

    public Role getRole(final String token) {
        String role = getBodyWithValidation(token)
                .get(CLAIM_ROLE, String.class);
        return Role.from(role);
    }

    private Claims getBodyWithValidation(final String token) {
        try {
            return parser.parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationException("사용할 수 없는 토큰입니다.");
        }
    }
}

