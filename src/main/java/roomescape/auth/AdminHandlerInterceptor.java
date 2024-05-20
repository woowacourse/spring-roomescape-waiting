package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.global.exception.auth.AuthorizationException;
import roomescape.global.util.CookieUtils;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Component
public class AdminHandlerInterceptor implements HandlerInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    public AdminHandlerInterceptor(JwtTokenProvider jwtTokenProvider, MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String accessToken = CookieUtils.getToken(request);
        Member findMember = memberService.findById(jwtTokenProvider.getAccessorId(accessToken));
        if (findMember.isAdmin()) {
            return true;
        }
        throw new AuthorizationException("접근 권한이 없습니다");
    }
}
