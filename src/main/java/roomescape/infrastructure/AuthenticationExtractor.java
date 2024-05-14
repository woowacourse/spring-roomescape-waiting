package roomescape.infrastructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthenticationException;

@Component
public class AuthenticationExtractor {

    public String extract(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationException();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .orElseThrow(AuthenticationException::new)
                .getValue();
    }
}
