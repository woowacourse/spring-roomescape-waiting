package roomescape.common.cookie.extractor;

import jakarta.servlet.http.Cookie;

import java.util.List;

public interface CookieExtractor {

    String execute(List<Cookie> cookies, String cookieName);
}
