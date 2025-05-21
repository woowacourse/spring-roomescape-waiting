package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.auth.AuthService;
import roomescape.application.auth.dto.MemberIdDto;

@Component
@Transactional
public class CheckAdminRoleInterceptor implements HandlerInterceptor {

    private final AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor;
    private final AuthService authService;

    public CheckAdminRoleInterceptor(
            AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor,
            AuthService authService
    ) {
        this.authenticatedMemberIdExtractor = authenticatedMemberIdExtractor;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            MemberIdDto memberIdDto = authenticatedMemberIdExtractor.extract(request);

            if (!authService.isAdminAuthorized(memberIdDto)) {
                response.setStatus(401);
                return false;
            }

            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}
