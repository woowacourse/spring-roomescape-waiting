package roomescape.common.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.exception.ForbiddenException;
import roomescape.common.security.exception.UnAuthorizedException;
import roomescape.common.security.infrastructure.AuthorizationExtractor;
import roomescape.common.security.infrastructure.JwtProvider;
import roomescape.member.domain.MemberRole;

public class RoleInterceptor implements HandlerInterceptor {

    private static final String ADMIN = "/admin";

    private final AuthorizationExtractor authorizationExtractor;
    private final JwtProvider jwtProvider;

    public RoleInterceptor(final AuthorizationExtractor authorizationExtractor, final JwtProvider jwtProvider) {
        this.authorizationExtractor = authorizationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith(ADMIN)) {
            return validateToken(request, MemberRole.ADMIN);
        }
        if (!(handler instanceof final HandlerMethod handlerMethod)) {
            return true;
        }
        return validateToken(request, handlerMethod);
    }

    private boolean validateToken(final HttpServletRequest request, final HandlerMethod handlerMethod) {
        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (classAnnotation != null) {
            return validateToken(request, classAnnotation.value());
        }
        if (methodAnnotation != null) {
            return validateToken(request, methodAnnotation.value());
        }
        return true;
    }

    private boolean validateToken(final HttpServletRequest request, final MemberRole memberRole) {
        String token = authorizationExtractor.extract(request);
        if (token == null) {
            throw new UnAuthorizedException("토큰이 존재하지 않습니다.");
        }
        if ((memberRole == MemberRole.ADMIN) && (jwtProvider.getRole(token) != MemberRole.ADMIN)) {
            throw new ForbiddenException("접근할 수 없습니다.");
        }
        return true;
    }
}
