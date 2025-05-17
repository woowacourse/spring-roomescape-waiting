package roomescape.common.cookie.setter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public interface CookieSetter {

    void execute(final HttpServletResponse response, Cookie cookie);
}
