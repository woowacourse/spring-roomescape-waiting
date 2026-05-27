package roomescape.common.auth.interceptor;

import static roomescape.common.auth.exception.AuthExceptionInformation.UN_AUTHORIZED;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.common.auth.exception.AuthException;
import roomescape.common.auth.jwt.JwtValidator;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtValidator jwtValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthGuard authGuard = handlerMethod.getMethodAnnotation(AuthGuard.class);
        if (authGuard == null) {
            return true;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException(UN_AUTHORIZED);
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty() || !jwtValidator.validateJwtToken(token)) {
            throw new AuthException(UN_AUTHORIZED);
        }

        request.setAttribute("token", token);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

}
