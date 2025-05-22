package roomescape.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.MemberRole;
import roomescape.global.dto.SessionMember;
import roomescape.global.exception.AccessDeniedException;
import roomescape.global.exception.AuthenticationException;

public class AdminOnlyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AuthenticationException("로그인이 필요합니다.");
        }

        SessionMember sessionMember = (SessionMember) session.getAttribute("LOGIN_MEMBER");
        if (sessionMember == null) {
            throw new AuthenticationException("로그인이 필요합니다.");
        }
        if (sessionMember.role() != MemberRole.ADMIN) {
            throw new AccessDeniedException("어드민이 아닙니다.");
        }
        return true;
    }
}
