package roomescape.global.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class CookieAuthorizationExtractor implements AuthorizationExtractor {

    @Override
    public String extract(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies)
                .filter(c -> "token".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
