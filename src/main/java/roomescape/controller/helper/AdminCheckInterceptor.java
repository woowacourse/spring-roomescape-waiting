package roomescape.controller.helper;

import static roomescape.global.Constants.TOKEN_NAME;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Role;
import roomescape.global.CookieUtils;
import roomescape.global.JwtManager;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    private static final String AUTHORITY = "role";

    private final JwtManager jwtManager;

    public AdminCheckInterceptor(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Optional<Cookie> cookie = CookieUtils.findCookie(request, TOKEN_NAME);

        if (cookie.isEmpty()) {
            response.sendRedirect("/login");
            return false;
        }

        Claims claims = jwtManager.verifyToken(cookie.get().getValue());
        String role = claims.get(AUTHORITY, String.class);

        if (Role.getRole(role) == Role.ADMIN) {
            return true;
        }

        response.sendRedirect("/");
        return false;
    }
}
