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
        String token = extractTokenFromCookie(request);

        if (token == null) {
            throw new NotFoundCookieException();
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new ForbiddenException("유효하지 않는 토큰입니다.");
        }

        Claims claims = jwtTokenProvider.getClaims(token);
        String roleStr = (String) claims.get("role");
        Role role = Role.valueOf(roleStr);

        if (!role.equals(Role.ROLE_ADMIN)) {
            throw new ForbiddenException();
        }
        return true;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_NAME_FILED.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
