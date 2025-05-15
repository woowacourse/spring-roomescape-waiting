package roomescape.presentation.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.application.support.exception.JwtExtractException;

@Component
public class JwtTokenExtractor {

    private final static String JWT_TOKEN_COOKIE_KEY = "token";

    public String extract(HttpServletRequest request) {
        return getTokenCookie(request).getValue();
    }

    private Cookie getTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new JwtExtractException("인증 쿠키값이 존재하지 않습니다.");
        }
        return Arrays.stream(request.getCookies())
                .filter(each -> each.getName().equals(JWT_TOKEN_COOKIE_KEY))
                .findFirst()
                .orElseThrow(() -> new JwtExtractException("인증 쿠키값이 존재하지 않습니다."));
    }
}
