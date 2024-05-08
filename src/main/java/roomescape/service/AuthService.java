package roomescape.service;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import roomescape.exception.AuthenticationException;
import roomescape.exception.BadRequestException;
import roomescape.model.Role;
import roomescape.model.User;

@Service
public class AuthService {

    private static final String COOKIE_NAME = "token";
    private final TokenProvider tokenProvider;

    public AuthService(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public Cookie createCookieByUser(User user) {
        String jwtToken = tokenProvider.createToken(user);
        Cookie cookie = new Cookie(COOKIE_NAME, jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Long findUserIdByCookie(Cookie[] cookies) {
        Cookie cookie = findCookieIfExist(cookies);
        Claims claims = tokenProvider.getPayload(cookie.getValue());
        String payload = claims.getSubject();
        validatePayload(payload);
        return Long.parseLong(payload);
    }

    public Role findRoleByCookie(Cookie[] cookies) {
        Cookie cookie = findCookieIfExist(cookies);
        Claims claims = tokenProvider.getPayload(cookie.getValue());
        String payload = claims.get("role", String.class);
        return Role.valueOf(payload);
    }

    private Cookie findCookieIfExist(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .findAny()
                .orElseThrow(() -> new BadRequestException("아이디가 %s인 쿠키가 없습니다.".formatted(COOKIE_NAME)));
    }

    private void validatePayload(String payLoad) {
        try {
            Long.parseLong(payLoad);
        } catch (NumberFormatException exception) {
            throw new AuthenticationException();
        }
    }
}
