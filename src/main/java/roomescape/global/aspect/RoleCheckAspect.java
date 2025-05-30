package roomescape.global.aspect;

import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import roomescape.domain.member.MemberRole;
import roomescape.global.annotation.CheckRole;
import roomescape.global.dto.SessionMember;
import roomescape.global.exception.AccessDeniedException;
import roomescape.global.exception.AuthenticationException;

@Aspect
@Component
public class RoleCheckAspect {

    private final HttpSession session;

    public RoleCheckAspect(final HttpSession session) {
        this.session = session;
    }

    @Before("@annotation(checkRole)")
    public void checkUserRole(CheckRole checkRole) {
        MemberRole role = checkRole.value();
        if (session == null) {
            throw new AuthenticationException("로그인이 필요합니다.");
        }
        SessionMember sessionMember = (SessionMember) session.getAttribute("LOGIN_MEMBER");
        if (sessionMember == null) {
            throw new AuthenticationException("로그인이 필요합니다.");
        }
        if (sessionMember.role() != role) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }
}
