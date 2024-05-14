package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Role;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;

@Component
public class AdminRoleHandlerInterceptor implements HandlerInterceptor {
    private static final String KEY = "token";

    private final TokenProvider tokenProvider;

    public AdminRoleHandlerInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        String token = extractTokenFromCookie(cookies);
        String role = tokenProvider.extractMemberRole(token);
        if (!Role.valueOf(role).isAdmin()) {
            throw new ForbiddenException("권한이 없는 접근입니다.");
        }
        return true;
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(KEY))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new UnauthorizedException("권한이 없는 접근입니다."));
    }
}
