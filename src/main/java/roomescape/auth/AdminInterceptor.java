package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.business.AdminAccessException;
import roomescape.exception.business.AuthException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

public class AdminInterceptor implements HandlerInterceptor {

    private final MemberService memberService;

    public AdminInterceptor(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("memberId") == null) {
            throw new AuthException();
        }
        Member member = memberService.getById((Long) session.getAttribute("memberId"));
        if (!member.isAdmin()) {
            throw new AdminAccessException();
        }
        return true;
    }
}
