package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import roomescape.domain.Member;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;
import roomescape.util.JwtProvider;

@Component
public class CheckRoleInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    CheckRoleInterceptor(JwtProvider jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = CookieUtil.requireNonnull(request.getCookies(), TokenValidationFailureException::new);
        String token = CookieUtil.extractToken(cookies).orElseThrow(TokenValidationFailureException::new);
        validateAdmin(token);
        return true;
    }

    private void validateAdmin(String token) {
        String subject = jwtProvider.getSubject(token);
        long memberId = Long.parseLong(subject);
        Member member = memberService.findValidatedSiteUserById(memberId);
        if (!member.getRole().isAdmin()) {
            throw new ForbiddenAccessException();
        }
    }
}
