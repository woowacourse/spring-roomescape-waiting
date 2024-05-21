package roomescape.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.user.Member;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;
import java.util.Date;

@Component
public class TokenProvider {
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public static class TokenResponse {
        private final long id;

        private TokenResponse(final String value) {
            this.id = Long.valueOf(value);
        }

        public long getId() {
            return id;
        }
    }

    public String generateToken(final Member member) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setSubject(member.getId()
                        .toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .claim("name", member.getName())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public TokenResponse decodeToken(final String token) {
        return new TokenResponse(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    public String parseToken(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(UnauthorizedException::new);
    }
}
