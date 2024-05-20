package roomescape.global.auth;

import jakarta.servlet.http.Cookie;

public class CookieGenerator {

    public static Cookie generate(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie generateEmptyCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
