package roomescape.config.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.entity.Member;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.service.AuthService;

public class CheckLoginInterceptor implements HandlerInterceptor {

    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_EMAIL = "admin";
    private static final String ADMIN_PASSWORD = "1234";

    private final AuthService authService;

    public CheckLoginInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String token = extractTokenFromCookie(request.getCookies());
        Member member = authService.findMemberByToken(token);

        if (!member.getName().equals(ADMIN_NAME)
            || !member.getEmail().equals(ADMIN_EMAIL)
            || !member.getPassword().equals(ADMIN_PASSWORD)) {
            response.setStatus(401);
            return false;
        }
        return true;
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        throw new AuthenticatedException("token error");
    }
}
