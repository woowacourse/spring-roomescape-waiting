package roomescape.presentation.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class AuthenticationTokenCookie extends Cookie {

    public static final String COOKIE_KEY = "token";

    private final String token;
    private final boolean hasToken;

    private AuthenticationTokenCookie(final String token, final boolean hasToken) {
        super(COOKIE_KEY, token);
        this.token = token;
        this.hasToken = hasToken;
    }

    public static AuthenticationTokenCookie fromRequest(final HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return new AuthenticationTokenCookie(null, false);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_KEY))
                .map(Cookie::getValue)
                .findAny()
                .map(token -> new AuthenticationTokenCookie(token, true))
                .orElse(new AuthenticationTokenCookie(null, false));
    }

    public static AuthenticationTokenCookie forResponse(final String token) {
        var cookie = new AuthenticationTokenCookie(token, true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static AuthenticationTokenCookie forExpire() {
        var cookie = new AuthenticationTokenCookie("", true);
        cookie.setMaxAge(0);
        return cookie;
    }

    public String token() {
        return token;
    }

    public boolean hasToken() {
        return hasToken;
    }
}
