package roomescape.config.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.entity.Member;
import roomescape.entity.Role;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.service.AuthService;

public class CheckLoginInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckLoginInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String token = extractTokenFromCookie(request.getCookies());
        Member member = authService.findMemberByToken(token);

        if (member == null || member.getRole() != Role.ADMIN) {
            throw new AuthenticatedException("관리자 권한 필요");
        }
        return true;
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new AuthenticatedException("token not found");
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        throw new AuthenticatedException("token error");
    }
}
