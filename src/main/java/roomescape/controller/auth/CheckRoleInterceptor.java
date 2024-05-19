package roomescape.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.dto.auth.LoginMember;

@Component
public class CheckRoleInterceptor implements HandlerInterceptor {

    private final CookieProvider cookieProvider;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CheckRoleInterceptor(final CookieProvider cookieProvider, final JwtTokenProvider jwtTokenProvider) {
        this.cookieProvider = cookieProvider;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final String accessToken = cookieProvider.extractToken(request.getCookies());
        final LoginMember loginMember = jwtTokenProvider.parse(accessToken);

        if (loginMember.isNotAdmin()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
