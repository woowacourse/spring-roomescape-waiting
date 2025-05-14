package roomescape.auth.web.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;
import roomescape.auth.web.cookie.CookieProvider;
import roomescape.auth.web.exception.NotAdminException;

@RequiredArgsConstructor
@Component
public class AdminMemberHandlerInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final CookieProvider cookieProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Cookie[] cookies = request.getCookies();
        String token = cookieProvider.extractTokenFromCookie(cookies);

        boolean isAdmin = authService.isAdmin(token);
        if (!isAdmin) {
            throw new NotAdminException("[ERROR] 관리자 권한이 없습니다.");
        }
        return true;
    }
}
