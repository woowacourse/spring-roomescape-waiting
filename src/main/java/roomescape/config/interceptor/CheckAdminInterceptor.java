package roomescape.config.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.global.ForbiddenException;
import roomescape.exception.local.NotFoundCookieException;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {

    private final CookieUtility cookieUtility;
    private final JwtTokenProvider jwtTokenProvider;

    public CheckAdminInterceptor(CookieUtility cookieUtility, JwtTokenProvider jwtTokenProvider) {
        this.cookieUtility = cookieUtility;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie accessTokenCookie = getAccessTokenCookie(request);
        AccessTokenContent tokenContent = jwtTokenProvider.parseAccessToken(accessTokenCookie.getValue());
        return canEnterUri(request.getRequestURI(), tokenContent);
    }

    private Cookie getAccessTokenCookie(HttpServletRequest request) {
        return cookieUtility.findCookie(request, "access")
                .orElseThrow(NotFoundCookieException::new);
    }

    private boolean canEnterUri(String uri, AccessTokenContent tokenContent) {
        boolean isAdminUri = uri.startsWith("/admin");
        if (isAdminUri) {
            if (tokenContent.isAdminToken()) {
                return true;
            }
            throw new ForbiddenException();
        }
        return true;
    }
}
