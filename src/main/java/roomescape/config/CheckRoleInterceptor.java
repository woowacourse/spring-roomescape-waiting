package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.MemberService;

public class CheckRoleInterceptor implements HandlerInterceptor {

    private final MemberService memberService;

    CheckRoleInterceptor(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            throw new ForbiddenAccessException("관리자만 접근 가능합니다.");
        }

        boolean isAdmin = false;
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                Member member = memberService.getUserByToken(cookie.getValue());
                isAdmin = member.getRole().equals(Role.ADMIN);
            }
        }

        if (!isAdmin) {
            throw new ForbiddenAccessException("관리자만 접근 가능합니다.");
        }
        return true;
    }
}
