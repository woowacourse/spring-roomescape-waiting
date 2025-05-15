package roomescape.auth.service;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import roomescape.common.exception.AuthenticationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Component
public class JwtTokenHandler {

    private static final String TOKEN_NAME = "token";
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

    public String extractTokenValue(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationException("쿠키가 존재하지 않습니다.");
        }
        return Arrays.stream(cookies)
                .filter(c -> TOKEN_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("토큰이 존재하지 않습니다."));
    }

    public String getMemberIdFromToken(final String token) {
        return getBodyWithValidation(token).getSubject();
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

