package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.global.exception.AuthorizationException;

@Component
public class JwtManager {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public String createToken(Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setSubject(member.getId().toString())
            .setExpiration(validity)
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .compact();
    }

    public Long parseToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthorizationException("로그인 후 이용하세요.");
        }
        return parse(extractToken(cookies));
    }

    private String extractToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private Long parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new AuthorizationException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException | MalformedJwtException | NumberFormatException e) {
            throw new AuthorizationException("잘못된 형식의 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new AuthorizationException("빈 토큰을 입력할 수 없습니다.");
        }
    }
}

