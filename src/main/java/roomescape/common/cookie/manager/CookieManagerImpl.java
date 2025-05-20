package roomescape.common.cookie.manager;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.common.cookie.extractor.CookieExtractor;
import roomescape.common.cookie.extractor.MissingCookieException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CookieManagerImpl implements CookieManager {

    private final CookieExtractor extractor;

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
