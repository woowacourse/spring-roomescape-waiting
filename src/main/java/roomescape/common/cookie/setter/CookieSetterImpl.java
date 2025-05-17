package roomescape.common.cookie.setter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieSetterImpl implements CookieSetter {

    @Override
    public void execute(final HttpServletResponse response,
                        final Cookie cookie) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response cannot be null");
        }
        if (cookie == null) {
            throw new IllegalArgumentException("cookie cannot be null");
        }
        response.addCookie(cookie);
    }
}
