package roomescape.login.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.impl.ForbiddenException;
import roomescape.common.exception.impl.UnauthorizedException;
import roomescape.login.application.JwtHandler;
import roomescape.login.application.TokenCookieService;
import roomescape.member.domain.Role;


@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtHandler jwtHandler;
    private final TokenCookieService tokenCookieService;

    public AdminInterceptor(final JwtHandler jwtHandler, final TokenCookieService tokenCookieService) {
        this.jwtHandler = jwtHandler;
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        final String accessToken = tokenCookieService.getTokenFromCookies(request.getCookies());
        final String roleName = jwtHandler.decode(accessToken, JwtHandler.CLAIM_ROLE_KEY);

        final Role role = Role.valueOf(roleName);
        if (role != Role.ADMIN) {
            throw new ForbiddenException("회원 권한이 존재하지 않아 접근할 수 없습니다.");
        }
        return true;
    }
}
