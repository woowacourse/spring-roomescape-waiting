package roomescape.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.CookieAuthorizationExtractor;
import roomescape.domain.Member;
import roomescape.service.MemberService;

@Component
public class CheckAdminRoleInterceptor implements HandlerInterceptor {
    private final AuthorizationExtractor<String> authorizationExtractor;
    private final MemberService memberService;

    public CheckAdminRoleInterceptor(final MemberService memberService) {
        this.authorizationExtractor = new CookieAuthorizationExtractor();
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = authorizationExtractor.extract(request);
        final Member member = memberService.getMemberInfo(accessToken);
        if (!member.isAdmin()) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
