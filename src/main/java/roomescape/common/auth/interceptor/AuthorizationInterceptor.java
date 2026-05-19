package roomescape.common.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.common.auth.exception.AuthException;
import roomescape.common.auth.jwt.JwtExtractor;
import roomescape.member.domain.Role;

import java.util.Arrays;

import static roomescape.common.auth.exception.AuthExceptionInformation.FORBIDDEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtExtractor jwtExtractor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthGuard authGuard = handlerMethod.getMethodAnnotation(AuthGuard.class);
        if (authGuard == null) {
            return true;
        }

        String token = (String) request.getAttribute("token");
        Long id = jwtExtractor.getId(token);
        log.info("Requester Id: " + id);
        request.setAttribute("memberId", id);

        Role[] permissionRoles = authGuard.roles();
        if (permissionRoles.length == 0) {
            return true;
        }

        Role requesterRole = Role.valueOf(jwtExtractor.getRole(token));
        boolean isAuthorized = Arrays.stream(permissionRoles)
                .anyMatch(role -> role == requesterRole);
        if (!isAuthorized) {
            throw new AuthException(FORBIDDEN);
        }

        return true;
    }
}
