package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.auth.TokenManager;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;

public class AdminRoleInterceptor implements HandlerInterceptor {
    private final TokenManager tokenManager;
    private final CredentialContext context;

    public AdminRoleInterceptor(TokenManager tokenManager, CredentialContext context) {
        this.tokenManager = tokenManager;
        this.context = context;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (context.hasCredential()) {
            context.validatePermission(Role.ADMIN);
            return true;
        }
        String token = AuthInformationExtractor.extractToken(request);
        MemberRole memberRole = tokenManager.extract(token);
        context.setCredentialIfNotPresent(memberRole);
        context.validatePermission(Role.ADMIN);
        return true;
    }
}
