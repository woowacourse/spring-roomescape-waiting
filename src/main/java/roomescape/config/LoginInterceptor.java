package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.InvalidAuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;
import roomescape.util.CookieTokenExtractor;
import roomescape.util.JwtTokenProvider;

public class LoginInterceptor implements HandlerInterceptor {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieTokenExtractor authorizationExtractor;

    public LoginInterceptor(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authorizationExtractor = new CookieTokenExtractor();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = authorizationExtractor.extract(request);
        if (token == null || token.isBlank()) {
            throw new InvalidAuthorizationException("[ERROR] 로그인이 필요합니다.");
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
        Member member = memberService.findMemberById(memberId);

        if (!Role.isAdmin(member.getRole())) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
