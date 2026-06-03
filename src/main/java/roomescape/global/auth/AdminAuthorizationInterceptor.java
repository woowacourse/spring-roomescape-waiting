package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.service.AuthService;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminAuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isAdminOnly(handler)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw unauthenticated();
        }

        Object memberId = session.getAttribute(AuthService.LOGIN_MEMBER_ID);
        if (!(memberId instanceof Long id)) {
            throw unauthenticated();
        }

        Member member = authService.getLoginMember(id);
        if (member.getRole() != Role.ADMIN) {
            throw new RoomescapeException(DomainErrorCode.UNAUTHORIZED_ADMIN, "관리자 권한이 필요합니다.");
        }

        return true;
    }

    private boolean isAdminOnly(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }

        return handlerMethod.hasMethodAnnotation(AdminOnly.class)
                || handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class);
    }

    private RoomescapeException unauthenticated() {
        return new RoomescapeException(DomainErrorCode.UNAUTHENTICATED, "로그인이 필요합니다.");
    }
}
