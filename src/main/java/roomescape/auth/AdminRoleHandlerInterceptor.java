package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Role;
import roomescape.exception.ForbiddenException;
import roomescape.util.CookieUtils;

@Component
public class AdminRoleHandlerInterceptor implements HandlerInterceptor {
    private final TokenProvider tokenProvider;

    public AdminRoleHandlerInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        String token = CookieUtils.extractTokenFromCookie(cookies);
        String role = tokenProvider.extractMemberRole(token);
        if (!Role.valueOf(role).isAdmin()) {
            throw new ForbiddenException();
        }
        return true;
    }
}
