package roomescape.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.member.service.MemberService;
import roomescape.util.TokenExtractor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final MemberService memberService;

    public LoginCheckInterceptor(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws AuthenticationException {
        String token = TokenExtractor.extractTokenFromCookie(request.getCookies());
        if (memberService.isAdminToken(token)) {
            return true;
        }
        throw new AuthenticationException("접근 권한이 없습니다.");
    }
}
