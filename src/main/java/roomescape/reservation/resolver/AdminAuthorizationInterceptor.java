package roomescape.reservation.resolver;

import static roomescape.member.role.Role.ADMIN;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.UnauthenticatedException;
import roomescape.member.service.AuthService;

public class AdminAuthorizationInterceptor implements HandlerInterceptor {
    public static final String TOKEN = "token";
    private final AuthService authService;

    public AdminAuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = extractTokenFromCookie(cookies);
        if (token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            MemberResponse member = authService.findUserByToken(token);

            if (!ADMIN.getRole().equals(member.role())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            return true;
        } catch (UnauthenticatedException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (TOKEN.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
