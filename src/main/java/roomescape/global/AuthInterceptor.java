package roomescape.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String SESSION_KEY = "id";
    private final MemberService memberService;

    public AuthInterceptor(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {

        final String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/admin")) {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute(SESSION_KEY) == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "[ERROR] 로그인이 필요합니다.");
                return false;
            }

            final Long memberId = (Long) session.getAttribute(SESSION_KEY);

            final Member member = memberService.getMemberById(memberId);

            if (MemberRole.ADMIN != member.getRole()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "[ERROR] 관리자 권한이 필요합니다.");
                return false;
            }
        }

        return true;
    }
}
