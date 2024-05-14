package roomescape.infrastructure.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.AuthService;
import roomescape.application.dto.MemberResponse;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class CheckLoginInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public CheckLoginInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = authService.extractToken(request.getCookies());
        MemberResponse memberResponse = authService.findMemberByToken(token);
        if (memberResponse.role().isAdmin()) {
            return true;
        }
        throw new RoomescapeException(RoomescapeErrorCode.FORBIDDEN,
                String.format("관리자 권한이 없는 사용자입니다. 사용자 id:%d", memberResponse.id()));
    }
}
