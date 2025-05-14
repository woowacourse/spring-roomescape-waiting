package roomescape.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;

@Component
public class CookieProvider {

    public ResponseCookie create(String token) {
        return ResponseCookie.from("token", token)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(24 * 60 * 60) //1일
                .build();
    }

    public ResponseCookie invalidate(Cookie cookie) {
        return ResponseCookie.from("token", cookie.getValue())
                .maxAge(0)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
    }

    public String extractTokenFromCookies(final Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizedException();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(UnauthorizedException::new);
    }

    public String extractTokenFromCookie(final Cookie cookie) {
        if (cookie == null) {
            throw new UnauthorizedException();
        }

        return cookie.getValue();
    }
}
