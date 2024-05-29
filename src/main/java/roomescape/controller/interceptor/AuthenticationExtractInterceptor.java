package roomescape.controller.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import roomescape.security.authentication.AnonymousAuthentication;
import roomescape.security.authentication.Authentication;
import roomescape.security.authentication.AuthenticationHolder;
import roomescape.service.AuthService;

@Component
public class AuthenticationExtractInterceptor implements HandlerInterceptor {

    private static final String AUTHENTICATION_KEY_NAME = "token";

    private final AuthService authService;

    public AuthenticationExtractInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = createAuthentication(request);
        AuthenticationHolder.setAuthentication(authentication);
        return true;
    }

    private Authentication createAuthentication(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, AUTHENTICATION_KEY_NAME);
        if (cookie == null) {
            return new AnonymousAuthentication();
        }
        String token = cookie.getValue();
        return authService.createAuthentication(token);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        AuthenticationHolder.clear();
    }
}
