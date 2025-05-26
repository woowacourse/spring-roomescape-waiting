package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
import roomescape.jwt.CookieManager;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;

public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final TokenProvider TokenProvider;
    private final MemberService memberService;

    public AdminAuthorizationInterceptor(TokenProvider TokenProvider, MemberService memberService) {
        this.TokenProvider = TokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        Long memberId = TokenProvider.getMemberIdFromToken(token);
        Member member = memberService.findById(memberId);
        if (!Role.isAdmin(member.getRole())) {
            redirectToLoginPage(response);
            return false;
        }
        return true;
    }

    private void redirectToLoginPage(HttpServletResponse response) {
        try {
            response.sendRedirect("/login");
        } catch (IOException e) {
            throw new RuntimeException("Redirect failed", e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        return CookieManager.extractByName("token", request)
                .orElseThrow(() -> new UnauthorizedException(ExceptionCause.UNAUTHORIZED_PAGE_ACCESS));
    }
}
