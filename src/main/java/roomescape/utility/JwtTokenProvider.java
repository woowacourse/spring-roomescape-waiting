package roomescape.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.global.AuthorizationException;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.token.secret-key}")
            String secretKey,
            @Value("${security.jwt.token.expire-length}")
            long validityInMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createAccessToken(AccessTokenContent accessTokenContent) {
        Map<String, Object> content = new HashMap<>();
        content.put("id", accessTokenContent.id());
        content.put("role", accessTokenContent.role());
        content.put("name", accessTokenContent.name());
        return makeToken(content);
    }

    public AccessTokenContent parseAccessToken(String accessToken) {
        try {
            Claims tokenPayload = parseToken(accessToken);
            return new AccessTokenContent(
                    tokenPayload.get("id", Long.class),
                    Role.valueOf(tokenPayload.get("role", String.class)),
                    tokenPayload.get("name", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("e.fillInStackTrace() = " + e.fillInStackTrace());
            throw new AuthorizationException("토큰 파싱 실패");
        }
    }

    private String makeToken(Map<String, Object> params) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        try {
            return Jwts.builder()
                    .claims(params)
                    .issuedAt(now)
                    .expiration(validity)
                    .signWith(secretKey)
                    .compact();
        } catch (JwtException e) {
            throw new AuthorizationException("토큰 생성 실패");
        }
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthorizationException("토큰 파싱 실패");
        }
    }
}
