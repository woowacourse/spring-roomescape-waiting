package roomescape.controller.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.CookieUtils;
import roomescape.exception.AuthorizationException;
import roomescape.service.AuthService;
import roomescape.service.dto.MemberInfo;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    public CheckAdminInterceptor(AuthService authService, CookieUtils cookieUtils) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = cookieUtils.extractToken(cookies);
        MemberInfo loginMember = authService.checkToken(token);
        if (loginMember.isNotAdmin()) {
            throw new AuthorizationException();
        }
        return true;
    }
}
