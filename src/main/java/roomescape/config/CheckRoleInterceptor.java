package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;

public class CheckRoleInterceptor implements HandlerInterceptor {

    private final MemberAuthService memberAuthService;
    private final MemberService memberService;

    public CheckRoleInterceptor(MemberAuthService memberAuthService, MemberService memberService) {
        this.memberAuthService = memberAuthService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = Objects.requireNonNull(request)
                .getCookies();
        if (memberAuthService.isLoginMember(cookies)) {
            MemberProfileInfo memberProfile = memberAuthService.extractPayload(cookies);
            Member member = memberService.findMemberById(memberProfile.id());
            return memberAuthService.isAdmin(member);
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

}
