package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.exception.UnauthorizedException;
import roomescape.reservation.exception.ReservationErrorCode;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        if (!hm.hasMethodAnnotation(Authorized.class)) {
            return true;
        }

        String name = request.getHeader("Authorization");

        if (name == null || name.isBlank()) {
            throw new UnauthorizedException(ReservationErrorCode.MISSING_AUTH_HEADER);
        }

        String decodedName = URLDecoder.decode(name, StandardCharsets.UTF_8);
        request.setAttribute("loginName", decodedName);

        return true;
    }
}
