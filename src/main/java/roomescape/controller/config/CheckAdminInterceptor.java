package roomescape.controller.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.utils.CookieUtils;
import roomescape.controller.utils.TokenUtils;
import roomescape.exception.AuthorizationException;
import roomescape.service.dto.MemberInfo;

public class CheckAdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = CookieUtils.extractToken(cookies);
        MemberInfo memberInfo = TokenUtils.parseToken(token);
        if (memberInfo.isNotAdmin()) {
            throw new AuthorizationException();
        }
        return true;
    }
}
