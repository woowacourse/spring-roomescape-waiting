package roomescape.global;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.service.exception.UnauthorizedException;

@Component
public class JwtManager {

    private static final String TOKEN = "token";

    private final String tokenSecretKey;
    private final long tokenExpirationPeriod;

    public JwtManager(@Value("${security.jwt.secret-key}") String tokenSecretKey,
                      @Value("${security.jwt.expiration-period}") long tokenExpirationPeriod) {
        this.tokenSecretKey = tokenSecretKey;
        this.tokenExpirationPeriod = tokenExpirationPeriod;
    }

    public String generateToken(Member member) {
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("name", member.getName())
                .claim("role", member.getRole().name())
                .signWith(Keys.hmacShaKeyFor(tokenSecretKey.getBytes()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationPeriod))
                .compact();
    }

    public String extractToken(HttpServletRequest request) {
        Optional<Cookie> cookie = CookieUtils.findCookie(request, TOKEN);
        if (cookie.isEmpty()) {
            throw new UnauthorizedException("권한이 없습니다. 로그인을 다시 시도해주세요.");
        }

        return cookie.get().getValue();
    }

    public Claims verifyToken(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException("기한이 만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new JwtException("JWT 토큰 구성이 올바르지 않습니다.");
        } catch (SignatureException e) {
            throw new JwtException("JWT 토큰 검증에 실패하였습니다.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(tokenSecretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
