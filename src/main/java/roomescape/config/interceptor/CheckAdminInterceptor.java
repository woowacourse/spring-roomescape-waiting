package roomescape.config.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.global.ForbiddenException;
import roomescape.exception.local.NotFoundCookieException;
import roomescape.utility.JwtTokenProvider;

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
                    AccessTokenContent accessTokenContent = jwtTokenProvider.parseAccessToken(cookie.getValue());
                    if (accessTokenContent.role() != Role.ROLE_ADMIN) {
                        throw new ForbiddenException();
                    }
                    return true;
                }
            }
        }
        throw new NotFoundCookieException();
    }
}
