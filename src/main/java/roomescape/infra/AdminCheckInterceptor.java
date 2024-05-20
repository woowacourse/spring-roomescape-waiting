package roomescape.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.dto.MemberInfo;
import roomescape.exception.RoomescapeException;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final MemberService memberService;

    public AdminCheckInterceptor(TokenService tokenService, MemberService memberService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        try {
            String token = TokenExtractor.extractFrom(request.getCookies());
            long memberId = tokenService.findMemberIdFromToken(token);
            MemberInfo memberInfo = memberService.findByMemberId(memberId);

            if (memberInfo.isAdmin()) {
                return true;
            }
            return blockUnauthorizedUser(response);
        } catch (RoomescapeException e) {
            return blockUnauthorizedUser(response);
        }
    }

    private boolean blockUnauthorizedUser(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
        return false;
    }
}
