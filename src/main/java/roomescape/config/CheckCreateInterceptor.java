package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.AuthService;
import roomescape.dto.MemberResponse;
import roomescape.exception.RoomescapeException;

@Component
public class CheckCreateInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public CheckCreateInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (method.equals("GET")) {
            return true;
        }
        Cookie[] cookies = request.getCookies();
        MemberResponse memberResponse = authService.findMemberByCookies(cookies);
        if (memberResponse.role().isAdmin()) {
            return true;
        }
        throw new RoomescapeException(HttpStatus.FORBIDDEN,
                String.format("관리자 권한이 없는 사용자입니다. 사용자 id:%d", memberResponse.id()));
    }
}
