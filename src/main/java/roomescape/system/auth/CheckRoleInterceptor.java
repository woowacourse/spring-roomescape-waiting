package roomescape.system.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Member;
import roomescape.service.MemberService;
import roomescape.system.exception.AuthorizationException;

@Component
public class CheckRoleInterceptor implements HandlerInterceptor {

    private final JwtManager jwtManager;
    private final MemberService memberService;

    public CheckRoleInterceptor(JwtManager jwtManager, MemberService memberService) {
        this.jwtManager = jwtManager;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        Long memberId = jwtManager.parseToken(request);
        Member member = memberService.findById(memberId);
        if (member.isNotAdmin()) {
            throw new AuthorizationException("접근 권한이 없습니다.");
        }
        return true;
    }
}
