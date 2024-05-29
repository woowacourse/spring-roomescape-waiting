package roomescape.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.security.authentication.Authentication;
import roomescape.security.authentication.AuthenticationHolder;
import roomescape.security.exception.AccessDeniedException;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = AuthenticationHolder.getAuthentication();
        if (authentication.isNotAdmin()) {
            throw new AccessDeniedException("어드민 권한이 필요합니다.");
        }
        return true;
    }
}
