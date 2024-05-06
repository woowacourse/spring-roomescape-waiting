package roomescape.presentation.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import roomescape.exception.AuthenticationInformationNotFoundException;

public class AuthInformationExtractor {
    private static final String COOKIE_NAME = "token";

    private AuthInformationExtractor() {
    }

    public static String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationInformationNotFoundException();
        }
        Cookie cookieWithProperty = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .findFirst()
                .orElseThrow(AuthenticationInformationNotFoundException::new);
        return cookieWithProperty.getValue();
    }
}
