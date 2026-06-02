package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.exception.UnauthenticatedException;
import roomescape.exception.UnauthorizedException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthorizationExtractor<String> authorizationExtractor = new BearerAuthorizationExtractor();
    private final JwtTokenProvider jwtProvider;

    public AuthInterceptor(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        AuthRequired authRequired = findAuthRequired(handlerMethod);
        if (authRequired == null) {
            return true;
        }

        String token = authorizationExtractor.extract(request);
        if (token == null) {
            throw new UnauthenticatedException();
        }
        Long userId = jwtProvider.getUserId(token);
        Role role = jwtProvider.getRole(token);

        validateRole(authRequired.roles(), role);

        request.setAttribute(AuthContext.LOGIN_USER_ID, userId);
        request.setAttribute(AuthContext.LOGIN_USER_ROLE, role);
        return true;
    }

    private AuthRequired findAuthRequired(HandlerMethod handlerMethod) {
        // 메타 어노테이션(@LoginRequired, @AdminOnly)까지 해석하려면
        // 기본 리플렉션이 아닌 AnnotatedElementUtils 를 사용해야 한다.
        AuthRequired onMethod = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), AuthRequired.class);
        if (onMethod != null) {
            return onMethod;
        }
        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), AuthRequired.class);
    }

    private void validateRole(Role[] requiredRoles, Role role) {
        if (requiredRoles.length == 0) {
            return;
        }
        if (Arrays.stream(requiredRoles).noneMatch(required -> required == role)) {
            throw new UnauthorizedException();
        }
    }
}