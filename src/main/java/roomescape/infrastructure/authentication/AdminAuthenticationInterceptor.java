package roomescape.infrastructure.authentication;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.AuthenticationService;
import roomescape.application.dto.MemberResponse;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class AdminAuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationService authenticationService;

    public AdminAuthenticationInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = authenticationService.extractToken(request.getCookies());
        MemberResponse memberResponse = authenticationService.findMemberByToken(token);
        if (memberResponse.role().isAdmin()) {
            return true;
        }
        throw new RoomescapeException(RoomescapeErrorCode.FORBIDDEN,
                String.format("관리자 권한이 없는 사용자입니다. 사용자 id:%d", memberResponse.id()));
    }
}
