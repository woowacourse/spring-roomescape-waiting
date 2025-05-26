package roomescape.auth.infrastructure.methodargument;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.MemberRole;

public class CheckMemberRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthorizationPayload authorizationPayload =
            (AuthorizationPayload) request.getAttribute("authorizationPayload");
        if (authorizationPayload == null) {
            throw new UnauthorizedException("로그인 정보가 없습니다.");
        }
        if (!authorizationPayload.role().equals(MemberRole.ADMIN)) {
            throw new ForbiddenException("관리자 권한이 필요합니다.");
        }
        return true;
    }
}
