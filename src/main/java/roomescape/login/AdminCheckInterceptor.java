package roomescape.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.login.dto.TokenResponse;
import roomescape.login.service.LoginService;
import roomescape.member.dto.MemberRequest;
import roomescape.member.service.MemberService;
import roomescape.util.TokenExtractor;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    private final LoginService loginService;
    private final MemberService memberService;

    public AdminCheckInterceptor(LoginService loginService, MemberService memberService) {
        this.loginService = loginService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws AuthenticationException {
        TokenResponse tokenResponse = TokenExtractor.extractTokenFromCookie(request.getCookies());
        MemberRequest memberRequest = loginService.getMemberRequestByToken(tokenResponse);

        if (memberService.isAdmin(memberRequest)) {
            return true;
        }
        throw new AuthenticationException("접근 권한이 없습니다.");
    }
}
