package roomescape.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.AuthTokenExtractor;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.domain.RequiresRole;
import roomescape.exception.auth.AuthenticationException;
import roomescape.exception.auth.AuthorizationException;

@RequiredArgsConstructor
public class AuthRoleCheckInterceptor implements HandlerInterceptor {

    private final AuthTokenExtractor<String> authTokenExtractor;
    private final AuthTokenProvider authTokenProvider;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, Object handler)
            throws AuthenticationException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresRole requiresRole;

        if ((requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class)) == null
                && (requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class)) == null) {
            return true;
        }

        final String accessToken = authTokenExtractor.extract(request);
        if (!authTokenProvider.validateToken(accessToken)) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }

        final AuthRole role = authTokenProvider.getRole(accessToken);
        if (Arrays.stream(requiresRole.authRoles())
                .noneMatch(authRole -> authRole == role)) {
            throw new AuthorizationException("권한이 없습니다.");
        }
        return true;
    }
}
