package roomescape.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;
import roomescape.common.exception.MissingTokenExcpetion;

@RequiredArgsConstructor
public class AdminRoleInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler) throws Exception {

        final String token = extractTokenFromCookies(request.getCookies());
        authService.validateAdminByToken(token);
        return true;
    }

    private String extractTokenFromCookies(final Cookie[] cookies) {
        if (cookies == null) {
            throw new MissingTokenExcpetion("Token is missing");
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }

        throw new MissingTokenExcpetion("Token is missing");
    }
}
