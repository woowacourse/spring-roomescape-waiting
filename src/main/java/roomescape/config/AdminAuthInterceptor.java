package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.LoginMember;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.service.LoginService;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final LoginService loginService;

    public AdminAuthInterceptor(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String accessToken = CookieExtractor.getTokenCookie(request).getValue();
        LoginMember loginMember = loginService.checkLogin(accessToken);
        if (!loginMember.isAdmin()) {
            throw new RoomescapeException(ExceptionType.PERMISSION_DENIED);
        }
        return true;
    }
}
