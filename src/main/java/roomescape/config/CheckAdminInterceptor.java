package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.AuthorizationException;
import roomescape.service.auth.AuthService;
import roomescape.service.dto.AuthInfo;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckAdminInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        AuthInfo authInfo = authService.getAuthInfo(request.getCookies());
        if (!authInfo.isAdmin()) {
            throw new AuthorizationException(
                    "관리자 권한이 없습니다. ID = " + authInfo.id()
                            + ", 이름 = " + authInfo.name()
                            + ", 현재 권한 = " + authInfo.role());
        }

        return true;
    }
}
