package roomescape.auth.config;

import static roomescape.exception.SecurityErrorCode.AUTHORITY_LACK;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.auth.Role;
import roomescape.auth.jwt.JwtUtil;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.auth.AuthenticationException;

public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthorizationInterceptor(final JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Role role = handlerMethod.getMethodAnnotation(Role.class);
        if (role == null) {
            return true;
        }
        AuthToken token = AuthToken.extractFrom(request);
        LoginInfo loginInfo = jwtUtil.validateAndResolveToken(token);
        validateUserRole(loginInfo, role);
        request.setAttribute("authorization", loginInfo);
        return true;
    }

    private static void validateUserRole(final LoginInfo loginInfo, final Role role) {
        UserRole userRole = loginInfo.userRole();
        final List<UserRole> allowedRoles = Arrays.asList(role.value());

        if (!allowedRoles.contains(userRole)) {
            throw new AuthenticationException(AUTHORITY_LACK);
        }
    }
}
