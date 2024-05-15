package roomescape.infra;

import static roomescape.exception.ExceptionType.PERMISSION_DENIED;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.dto.UserInfo;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = TokenExtractor.extractFrom(request.getCookies());
        long userIdFromToken = tokenService.findUserIdFromToken(token);
        UserInfo userInfo = memberService.findByUserId(userIdFromToken);
        if (!userInfo.role().equals(Role.ADMIN.name())) {
            throw new RoomescapeException(PERMISSION_DENIED);
        }
        return true;
    }
}
