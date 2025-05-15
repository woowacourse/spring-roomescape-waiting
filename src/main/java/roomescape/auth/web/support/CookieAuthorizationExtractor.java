package roomescape.auth.web.support;

import static roomescape.auth.web.constant.AuthConstant.AUTH_COOKIE_KEY;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.global.exception.AuthenticationException;
import roomescape.global.util.CookieUtils;

@Component
public class CookieAuthorizationExtractor implements AuthorizationExtractor<String> {

    public String extract(HttpServletRequest request) {
        Cookie cookie = CookieUtils.findFromCookiesByName(request.getCookies(), AUTH_COOKIE_KEY)
                .orElseThrow(() -> new AuthenticationException("인증을 위한 쿠키가 존재하지 않습니다."));
        return cookie.getValue();
    }
}
