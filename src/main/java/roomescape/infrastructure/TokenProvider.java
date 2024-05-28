package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.entity.Member;
import roomescape.domain.Role;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;

@Component
public class TokenProvider {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String AUTHENTICATION_PAYLOAD = "name";
    private static final String AUTHENTICATION_ROLE = "role";

    private final SecretKey secretKey;

    public TokenProvider(@Value("${jwt.secret.random.value}") String randomValue) {
        secretKey = Keys.hmacShaKeyFor(randomValue.getBytes());
    }

    public String generateTokenOf(Member member) {
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim(AUTHENTICATION_PAYLOAD, member.getName())
                .claim(AUTHENTICATION_ROLE, member.getRole())
                .signWith(secretKey)
                .compact();
    }

    public String extractAuthInfo(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        return extractAuthInfoFromToken(token);
    }

    private String extractAuthInfoFromToken(String token) {
        String authenticationInfo = parsePayload(token).get(AUTHENTICATION_PAYLOAD, String.class);
        if (authenticationInfo == null) {
            throw new CustomException(ExceptionCode.NO_AUTHENTICATION_INFO);
        }
        return authenticationInfo;
    }

    public Role parseAuthenticationRoleFromCookies(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        return parseAuthenticationRole(token);
    }

    private Role parseAuthenticationRole(String token) {
        String authenticationInfo = parsePayload(token).get(AUTHENTICATION_ROLE, String.class);
        if (authenticationInfo == null) {
            throw new CustomException(ExceptionCode.NO_AUTHENTICATION_INFO);
        }
        return Role.of(authenticationInfo);
    }

    public long parseMemberIdFromCookies(Cookie[] cookies) {
        String token = extractTokenFromCookie(cookies);
        return parseMemberId(token);
    }

    private long parseMemberId(String token) {
        String authenticationInfo = parsePayload(token).getSubject();
        if (authenticationInfo == null) {
            throw new CustomException(ExceptionCode.NO_AUTHENTICATION_INFO);
        }
        return Long.parseLong(authenticationInfo);
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new CustomException(ExceptionCode.NO_AUTHENTICATION_ACCESS);
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME))
                .findAny()
                .orElseThrow(() -> new CustomException(ExceptionCode.NO_AUTHENTICATION_ACCESS))
                .getValue();
    }

    private Claims parsePayload(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean doesNotHasCookie(final Cookie[] cookies) {
        return cookies.length == 0;
    }

    public boolean doesNotHasToken(final Cookie[] cookies) {
        return Arrays.stream(cookies)
                .noneMatch(cookie -> TOKEN_COOKIE_NAME.equals(cookie.getName()));
    }
}
