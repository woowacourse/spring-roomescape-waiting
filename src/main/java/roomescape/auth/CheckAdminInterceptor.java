package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.MemberRole;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final CookieProvider cookieProvider;
    private final JwtTokenProvider jwtTokenProvider;

    public CheckAdminInterceptor(final CookieProvider cookieProvider, final JwtTokenProvider jwtTokenProvider) {
        this.cookieProvider = cookieProvider;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        String token = cookieProvider.extractTokenFromCookies(request.getCookies());

        MemberRole role = jwtTokenProvider.extractMemberRoleFromToken(token);
        if (role == MemberRole.ADMIN) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"접근 권한이 없습니다.\"}");
        return false;
    }
}
