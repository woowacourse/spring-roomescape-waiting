package roomescape.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.auth.AuthenticationException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static roomescape.exception.SecurityErrorCode.TOKEN_EXPIRED;
import static roomescape.exception.SecurityErrorCode.TOKEN_INVALID;

@Component
public class JJWTJwtUtil implements JwtUtil {

    private static final String USER_ROLE_CLAIM_NAME = "role";

    private final SecretKey key;
    private final long expirationMills;

    public JJWTJwtUtil(@Value("${jwt.secret}") final String secret, @Value("${jwt.expirationMinute}") final long expirationMinute) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMills = expirationMinute * 60 * 1000;
    }

    @Override
    public AuthToken createToken(final User user) {
        String tokenValue = Jwts.builder()
                .subject(user.getId().value())
                .claim(USER_ROLE_CLAIM_NAME, user.getUserRole().name())
                .expiration(calculateExp())
                .signWith(key)
                .compact();

        return new AuthToken(tokenValue);
    }

    private Date calculateExp() {
        return new Date(new Date().getTime() + expirationMills);
    }

    @Override
    public LoginInfo validateAndResolveToken(final AuthToken token) {
        try {
            final Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token.value())
                    .getPayload();

            final String id = claims.getSubject();
            final UserRole role = UserRole.valueOf(claims.get(USER_ROLE_CLAIM_NAME, String.class));

            return new LoginInfo(id, role);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException(TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new AuthenticationException(TOKEN_INVALID);
        }
    }
}
