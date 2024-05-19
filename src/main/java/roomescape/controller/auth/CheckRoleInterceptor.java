package roomescape.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.CookieProvider;
import roomescape.domain.member.Role;
import roomescape.dto.MemberResponse;
import roomescape.service.MemberService;

@Component
public class CheckRoleInterceptor implements HandlerInterceptor {

    private final MemberService memberService;
    private final CookieProvider cookieProvider;

    @Autowired
    public CheckRoleInterceptor(final MemberService memberService,
                                final CookieProvider cookieProvider) {
        this.memberService = memberService;
        this.cookieProvider = cookieProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final String accessToken = cookieProvider.extractToken(request.getCookies());
        final MemberResponse memberResponse = memberService.findMemberByToken(accessToken);

        if (Role.isNotAdmin(memberResponse.role())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
