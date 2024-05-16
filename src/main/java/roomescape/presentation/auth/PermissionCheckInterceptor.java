package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.auth.TokenManager;

public class PermissionCheckInterceptor implements HandlerInterceptor {
    private final TokenManager tokenManager;
    private final CredentialContext context;

    public PermissionCheckInterceptor(TokenManager tokenManager, CredentialContext context) {
        this.tokenManager = tokenManager;
        this.context = context;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (!method.isAnnotationPresent(PermissionRequired.class)) {
            return true;
        }
        PermissionRequired permissionRequired = method.getAnnotation(PermissionRequired.class);
        String token = AuthInformationExtractor.extractToken(request);
        context.setCredentialIfNotPresent(tokenManager.extract(token));
        context.validatePermission(permissionRequired.role());
        return true;
    }
}
