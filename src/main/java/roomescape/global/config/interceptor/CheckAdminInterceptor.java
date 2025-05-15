package roomescape.global.config.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.exception.NotFoundCookieException;
import roomescape.global.exception.ForbiddenException;
import roomescape.user.domain.Role;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {

    private static final String TOKEN_NAME_FILED = "token";

    private final JwtTokenProvider jwtTokenProvider;

    public CheckAdminInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // token이 존재하고 token의 role 이 ADMIN
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_NAME_FILED.equals(cookie.getName())) {
                    Claims claims = jwtTokenProvider.getClaims(cookie.getValue());
                    String roleStr = (String) claims.get("role");
                    Role role = Role.valueOf(roleStr);
                    boolean equals = role.equals(Role.ROLE_ADMIN);
                    if (!equals) {
                        throw new ForbiddenException();
                    }
                    return true;
                }
            }
        }
        throw new NotFoundCookieException();
    }
}
