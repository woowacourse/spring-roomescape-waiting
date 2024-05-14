package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    public static final String COOKIE_NAME = "token";
    public static final String ADMIN = "ADMIN";
    private final String secretKey = "secret-token-test";
    private final long validityInMilliseconds = 3600000;

    public String createToken(String payload, String role) {
        Claims claims = Jwts.claims().setSubject(payload);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getPayload(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public String getTokenFromCookies(final HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("로그인 토큰이 없습니다"));
    }

    public void validateTokenRole(final String token) {
        final Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        final boolean isAdmin = ADMIN.equals(claims.getBody().get("role"));

        if (!isAdmin) {
            throw new JwtException("해당 기능에 접근하려면 관리자 권한이 필요합니다.");
        }
    }
}
