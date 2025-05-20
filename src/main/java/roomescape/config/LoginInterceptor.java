package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;
import roomescape.util.CookieTokenExtractor;
import roomescape.util.TokenProvider;

public class LoginInterceptor implements HandlerInterceptor {

    private final MemberService memberService;
    private final TokenProvider TokenProvider;
    private final CookieTokenExtractor authorizationExtractor;

    public LoginInterceptor(MemberService memberService, TokenProvider TokenProvider) {
        this.memberService = memberService;
        this.TokenProvider = TokenProvider;
        this.authorizationExtractor = new CookieTokenExtractor();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = authorizationExtractor.extract(request);
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException(ExceptionCause.JWT_TOKEN_EMPTY);
        }

        Long memberId = TokenProvider.getMemberIdFromToken(token);
        Member member = memberService.findMemberById(memberId);

        if (!Role.isAdmin(member.getRole())) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
