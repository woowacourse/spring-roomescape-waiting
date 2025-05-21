package roomescape.auth.infrastructure;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.auth.config.CookieProperties;

@Component
@EnableConfigurationProperties(CookieProperties.class)
public class CookieManager {

    private final CookieProperties cookieProperties;

    public CookieManager(final CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public ResponseCookie makeCookie(final String name, final String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .domain(cookieProperties.getDomain())
                .maxAge(cookieProperties.getMaxAge())
                .build();
    }

    public void deleteCookie(final HttpServletResponse response, final String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .domain(cookieProperties.getDomain())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
