package roomescape.member.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.AuthorizationException;
import roomescape.member.auth.jwt.JwtTokenExtractor;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.domain.Role;
import roomescape.member.service.AuthService;

@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (handlerMethod.hasMethodAnnotation(PermitAll.class)) {
            return true;
        }

        final String token = jwtTokenExtractor.extractTokenFromCookie(request.getCookies());
        MemberInfo memberInfo = authService.getMemberInfo(token);

        if (handlerMethod.hasMethodAnnotation(RoleRequired.class)) {
            RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
            validateRole(roleRequired.value(), memberInfo);
        }

        return true;
    }

    private void validateRole(Role[] roles, MemberInfo memberInfo) {
        if (Arrays.stream(roles).noneMatch(requiredRole -> requiredRole.isEqual(memberInfo.role()))) {
            throw new AuthorizationException();
        }
    }
}
