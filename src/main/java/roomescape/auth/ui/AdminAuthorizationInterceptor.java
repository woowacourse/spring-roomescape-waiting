package roomescape.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.application.TokenProvider;
import roomescape.auth.exception.AccessForbiddenException;
import roomescape.member.domain.Role;

@Component
@AllArgsConstructor
public class AdminAuthorizationInterceptor implements HandlerInterceptor {
    private final TokenProvider tokenProvider;
    private final CookieProvider cookieProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = cookieProvider.extractTokenFromCookie(request.getCookies());

        Role role = Role.findBy(tokenProvider.getRoleName(token));
        if (role != Role.ADMIN) {
            throw new AccessForbiddenException();
        }
        return true;
    }
}
