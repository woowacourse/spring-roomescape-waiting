package roomescape.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.entity.LoginMember;
import roomescape.global.Role;
import roomescape.exception.custom.ForbiddenException;
import roomescape.jwt.JwtExtractor;
import roomescape.service.AuthService;

@Component
public class AdminHandlerInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminHandlerInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = JwtExtractor.extractFromRequest(request);
        LoginMember loginMember = authService.getLoginMemberByToken(token);
        if (loginMember.getRole() == Role.ADMIN) {
            return true;
        }
        throw new ForbiddenException("어드민만 접근 가능한 페이지입니다.");
    }
}
