package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.business.service.AuthService;
import roomescape.exception.UnauthorizedException;
import roomescape.presentation.dto.LoginMember;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckAdminInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String accessToken = getAccessToken(request);
        final LoginMember loginMember = authService.getLoginMemberByAccessToken(accessToken);
        if (loginMember == null || !loginMember.role()
                .equalsIgnoreCase("ADMIN")) {
            throw new UnauthorizedException("접근 권한이 부족합니다.");
        }
        return true;
    }

    private String getAccessToken(final HttpServletRequest request) throws UnauthorizedException {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("로그인 정보가 없습니다.");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName()
                        .equals("token"))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("로그인 정보가 없습니다."))
                .getValue();
    }
}
