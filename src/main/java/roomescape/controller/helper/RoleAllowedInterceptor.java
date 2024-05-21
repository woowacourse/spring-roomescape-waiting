package roomescape.controller.helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.MemberRole;
import roomescape.exception.login.AccessDeniedException;
import roomescape.service.LoginService;

public class RoleAllowedInterceptor implements HandlerInterceptor {
    private final LoginService loginService;
    private final CookieExtractor cookieExtractor;

    public RoleAllowedInterceptor(LoginService loginService, CookieExtractor cookieExtractor) {
        this.loginService = loginService;
        this.cookieExtractor = cookieExtractor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RoleAllowed.class)) {
            checkRoleAccess(method, request);
        }
        return true;
    }

    private void checkRoleAccess(Method method, HttpServletRequest request) {
        RoleAllowed annotation = method.getAnnotation(RoleAllowed.class);
        MemberRole roleAllowed = annotation.value();

        String token = cookieExtractor.getToken(request.getCookies());
        MemberRole currentRole = loginService.findMemberRoleByToken(token);

        if (currentRole.isLowerThan(roleAllowed)) {
            throw new AccessDeniedException();
        }
    }
}
