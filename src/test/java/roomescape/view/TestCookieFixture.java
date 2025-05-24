package roomescape.view;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Role;

@Component
public class TestCookieFixture {

    private static final String COOKIE_NAME = "token";
    private static final long TOKEN_VALIDITY_DURATION = 1000 * 60 * 60;
    private static final Long TEST_MEMBER_ID = 1L;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    public Cookie createCookieWith(final Role role) {
        String token = createTestToken(role);
        return new Cookie(COOKIE_NAME, token);
    }

    private String createTestToken(final Role role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + TOKEN_VALIDITY_DURATION);

        return Jwts.builder()
                .claim("id", TEST_MEMBER_ID)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
