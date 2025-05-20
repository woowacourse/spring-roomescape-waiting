package roomescape.auth.infrastructure;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieManager {

    public ResponseCookie makeCookie(final String name, final String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public void deleteCookie(final HttpServletResponse response, final String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
