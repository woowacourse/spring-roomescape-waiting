package roomescape.config.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.controller.TokenCookieManager;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.exception.AdminAuthorizationException;
import roomescape.auth.service.AuthService;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {
    private final TokenCookieManager tokenCookieManager;
    private final AuthService authService;

    public AdminAuthorizationInterceptor(TokenCookieManager tokenCookieManager, AuthService authService) {
        this.tokenCookieManager = tokenCookieManager;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = tokenCookieManager.getToken(request.getCookies());
        LoggedInMember member = authService.findLoggedInMember(token);
        if (member.isAdmin()) {
            return true;
        }
        throw new AdminAuthorizationException();
    }
}
