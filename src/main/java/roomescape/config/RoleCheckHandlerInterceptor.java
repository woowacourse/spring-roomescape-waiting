package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.service.TokenCookieService;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Role;

@Component
public class RoleCheckHandlerInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCookieService tokenCookieService;

    public RoleCheckHandlerInterceptor(JwtTokenProvider jwtTokenProvider, TokenCookieService tokenCookieService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("사용자 인증 정보가 없습니다.");
        }

        String accessToken = tokenCookieService.getTokenFromCookies(request.getCookies());
        String roleName = jwtTokenProvider.decode(accessToken, JwtTokenProvider.CLAIM_ROLE_KEY);

        Role role = Role.valueOf(roleName);
        checkAdminRole(role);

        return true;
    }

    private void checkAdminRole(Role role) {
        if (!role.isAdminRole()) {
            throw new ForbiddenException("허용되지 않는 사용자입니다.");
        }
    }
}
