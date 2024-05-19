package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Member;
import roomescape.exception.ForbiddenException;
import roomescape.service.member.MemberService;
import roomescape.util.CookieUtils;

@Component
public class MemberHandlerInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    @Autowired
    public MemberHandlerInterceptor(TokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = CookieUtils.extractTokenFromCookie(request.getCookies());
        long memberId = tokenProvider.extractMemberId(token);
        Member member = memberService.findById(memberId);
        if (member.isGuest() || member.isAdmin()) {
            return true;
        }
        throw new ForbiddenException();
    }
}
