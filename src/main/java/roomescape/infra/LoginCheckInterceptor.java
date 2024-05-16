package roomescape.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.RoomescapeException;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final MemberService memberService;

    public LoginCheckInterceptor(TokenService tokenService, MemberService memberService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        try {
            String token = TokenExtractor.extractFrom(request.getCookies());
            long memberIdFromToken = tokenService.findMemberIdFromToken(token);
            memberService.findByMemberId(memberIdFromToken);
        } catch (RoomescapeException e) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
