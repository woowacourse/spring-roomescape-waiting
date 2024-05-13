package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.exception.AuthorizationException;
import roomescape.controller.member.dto.LoginMember;

public class CheckAdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler) {
        final LoginMember loginMember = (LoginMember) request.getAttribute("loginMember");
        if (!loginMember.isAdmin()) {
            throw new AuthorizationException("어드민만 접근할 수 있습니다.");
        }
        return true;
    }
}
