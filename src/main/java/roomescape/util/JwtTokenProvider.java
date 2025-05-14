package roomescape.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.exception.UnauthorizationException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtTokenProvider {
    private final static String ISSUER = "roomescape";

    @Value("${jwt.secret}")
    private String key;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Member member) {
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("name", member.getName())
                .claim("role", member.getRole().toString())
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(secretKey)
                .compact();
    }

    public Long extractId(String token) {
        try {
            String stringId = parseClaims(token).getSubject();
            return Long.parseLong(stringId);
        } catch (NumberFormatException e) {
            throw new UnauthorizationException("[ERROR] 잘못된 사용자 ID 형식입니다.");
        }
    }

    public Role extractRole(String token) {
        String stringRole = parseClaims(token).get("role", String.class);
        return Role.valueOf(stringRole);
    }

    private Claims parseClaims(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            Claims claims = jws.getPayload();
            if (claims.getExpiration().before(new Date())) {
                throw new UnauthorizationException("[ERROR] 토큰이 만료되었습니다.");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizationException("[ERROR] 유효하지 않은 토큰입니다.");
        }
    }

    public String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizationException("[ERROR] 쿠키가 존재하지 않습니다.");
        }
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), "token")) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizationException("[ERROR] 접근 권한이 없습니다.");
    }
}
