package roomescape.global.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.global.auth.exception.ForbiddenException;
import roomescape.global.auth.exception.UnAuthorizedException;
import roomescape.global.auth.infrastructure.AuthorizationExtractor;
import roomescape.global.auth.infrastructure.JwtProvider;
import roomescape.member.domain.MemberRole;

public class RoleInterceptor implements HandlerInterceptor {

    private final AuthorizationExtractor authorizationExtractor;
    private final JwtProvider jwtProvider;

    public RoleInterceptor(final AuthorizationExtractor authorizationExtractor, final JwtProvider jwtProvider) {
        this.authorizationExtractor = authorizationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin/")) {
            return validateAdminToken(request, MemberRole.ADMIN);
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> declaringClass = handlerMethod.getMethod().getDeclaringClass();
        if (!declaringClass.isAnnotationPresent(RequireRole.class)) {
            return true;
        }

        MemberRole memberRole = declaringClass.getAnnotation(RequireRole.class).value();
        validateAdminToken(request, memberRole);
        return true;
    }

    private boolean validateAdminToken(final HttpServletRequest request, final MemberRole memberRole) {
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
