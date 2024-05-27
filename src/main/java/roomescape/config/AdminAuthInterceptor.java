package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.AdminOnly;
import roomescape.dto.LoginMemberRequest;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if(!requiredAdmin(handlerMethod)){
            return true;
        }
        String accessToken = CookieExtractor.getTokenCookie(request).getValue();
        LoginMemberRequest loginMemberRequest = loginService.checkLogin(accessToken);
        if (!loginMemberRequest.role().isAdmin()) {
            throw new RoomescapeException(ExceptionType.PERMISSION_DENIED);
        }
        return true;
    }

    private boolean requiredAdmin(HandlerMethod handlerMethod) {
        if (handlerMethod.hasMethodAnnotation(AdminOnly.class)) {
            return true;
        }
        if (handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class)) {
            return true;
        }
        return false;
    }
}
