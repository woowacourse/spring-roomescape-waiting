package roomescape.config;

import java.util.Arrays;
import java.util.EnumSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Role;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.service.AuthService;

@RequiredArgsConstructor
public class AdminHandlerInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie tokenCookie = extractCookie(request, "token");
        EnumSet<Role> authorizedPermissions = EnumSet.of(Role.ADMIN);

        return authService.verifyPermission(tokenCookie.getValue(), authorizedPermissions);
    }

    private Cookie extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return new Cookie("token", "");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElseThrow(AuthenticationFailureException::new);
    }

}
