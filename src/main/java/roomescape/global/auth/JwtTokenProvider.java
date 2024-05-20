package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.global.exception.AuthorizationException;
import roomescape.global.exception.EscapeApplicationException;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInSeconds;

    public String generateToken(String payload) {
        Claims claims = Jwts.claims().setSubject(payload);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plusSeconds(validityInSeconds);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(validity.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter((cookie -> cookie.getName().equals("token")))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new EscapeApplicationException("token이 존재하지 않는 쿠키입니다."));
    }

    public Long validateAndGetLongSubject(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthorizationException("로그인 해야 합니다.");
        }
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
