package roomescape.common.cookie.manager;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.common.cookie.extractor.CookieExtractor;
import roomescape.common.cookie.extractor.MissingCookieException;
import roomescape.common.cookie.setter.CookieSetter;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CookieManagerImpl implements CookieManager {

    private final CookieExtractor extractor;
    private final CookieSetter setter;

    @Override
    public void setCookie(final HttpServletResponse response, final Cookie cookie) {
        setter.execute(response, cookie);
    }

    @Override
    public String extractCookie(final HttpServletRequest request, final String cookieName) {
        try {
            return extractor.execute(
                    List.of(request.getCookies()), cookieName);
        } catch (final Exception e) {
            throw new MissingCookieException(cookieName);
        }
    }
}
