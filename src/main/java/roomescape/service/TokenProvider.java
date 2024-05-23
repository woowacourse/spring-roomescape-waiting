package roomescape.service;

import static roomescape.controller.AuthController.TOKEN_COOKIE_NAME;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.exception.BadRequestException;
import roomescape.service.dto.AuthInfo;

@Component
public class TokenProvider {

    private final String secretKey;
    private final Long tokenExpirationMills;

    private static final String NAME_KEY = "name";
    private static final String ROLE_KEY = "role";

    public TokenProvider(
            @Value("${security.jwt.token.secret-key}") String secretKey,
            @Value("${security.jwt.token.expiration-millis}") Long tokenExpirationMills) {
        this.secretKey = secretKey;
        this.tokenExpirationMills = tokenExpirationMills;
    }

    public String createToken(Member member) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + tokenExpirationMills);

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim(NAME_KEY, member.getName())
                .claim(ROLE_KEY, member.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public AuthInfo getAuthInfo(Cookie[] cookies) {
        Claims claims = getClaims(extractTokenBy(cookies));

        Long id = Long.valueOf(claims.getSubject());
        String name = claims.get(NAME_KEY).toString();
        Role role = Role.findBy(claims.get(ROLE_KEY).toString());

        return new AuthInfo(id, name, role);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String extractTokenBy(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new BadRequestException("올바르지 않은 토큰값입니다."));
    }
}
