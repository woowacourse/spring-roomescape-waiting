package roomescape.auth.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieManager {

    @Value("${cookie.domain}")
    private String domain;

    @Value("${cookie.max-age}")
    private long maxAge;

    public ResponseCookie makeCookie(final String name, final String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .domain(domain)
                .maxAge(maxAge)
                .build();
    }

    public void deleteCookie(final HttpServletResponse response, final String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .domain(domain)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
