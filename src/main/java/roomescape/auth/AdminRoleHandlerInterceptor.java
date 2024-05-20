package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Role;
import roomescape.exception.ForbiddenException;
import roomescape.util.CookieUtil;

@Component
public class AdminRoleHandlerInterceptor implements HandlerInterceptor {
    private static final String AUTH_COOKIE_NAME = "auth_token";

    private final TokenProvider tokenProvider;

    public AdminRoleHandlerInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean isRequestFromAdmin = CookieUtil.searchValueFromKey(request.getCookies(), AUTH_COOKIE_NAME)
                .map(tokenProvider::extractMemberRole)
                .map(Role::valueOf)
                .map(Role::isAdmin)
                .orElse(Boolean.FALSE);

        if (!isRequestFromAdmin) {
            throw new ForbiddenException("해당 페이지에 접근할 수 있는 계정으로 로그인하지 않았습니다.");
        }
        return true;
    }
}
