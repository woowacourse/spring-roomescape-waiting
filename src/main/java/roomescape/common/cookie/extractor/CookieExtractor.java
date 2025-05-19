package roomescape.common.cookie.extractor;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CookieExtractor {

    public String execute(final List<Cookie> cookies, final String cookieName) {
        return cookies.stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findAny()
                .orElseThrow(() -> new MissingCookieException(cookieName));
    }
}
