package roomescape.presentation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.LoginMember;
import roomescape.domain.Role;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@RequiredArgsConstructor
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {

        if (!cookieUtils.containsCookieForToken(request)) {
            response.setStatus(401);
            return false;
        }

        final String token = cookieUtils.getToken(request);

        final LoginMember loginMember = authService.getMemberByToken(token);
        if (Role.isAdmin(loginMember.getRole())) {
            return true;
        }
        response.setStatus(401);
        return false;
    }
}
