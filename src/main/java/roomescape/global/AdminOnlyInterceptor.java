package roomescape.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.repository.MemberRepository;

public class AdminOnlyInterceptor implements HandlerInterceptor {
    private final MemberRepository memberRepository;

    public AdminOnlyInterceptor(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        SessionMember member = (SessionMember) session.getAttribute("LOGIN_MEMBER");
        if (member == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Member foundMember = memberRepository.findById(member.id())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 멤버입니다."));
        if (foundMember.getRole() != MemberRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return false;
        }
        return true;
    }
}
