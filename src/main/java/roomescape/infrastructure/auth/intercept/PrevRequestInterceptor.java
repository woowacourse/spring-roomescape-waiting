package roomescape.infrastructure.auth.intercept;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.infrastructure.auth.jwt.JwtCookieResolver;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.infrastructure.auth.member.UserInfo;

public class PrevRequestInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final List<RequestMatcher> adminRoleRequestMatcher;

    public PrevRequestInterceptor(JwtTokenProvider jwtTokenProvider, List<RequestMatcher> adminRoleRequestMatcher) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminRoleRequestMatcher = adminRoleRequestMatcher;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!isMatchExist(request)) {
            return true;
        }

        String token = JwtCookieResolver.getTokenFromCookie(request);
        UserInfo userInfo = jwtTokenProvider.resolveToken(token);

        if (!userInfo.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private boolean isMatchExist(HttpServletRequest request) {
        return adminRoleRequestMatcher.stream()
                .anyMatch(requestMatcher -> requestMatcher.isMatch(request));
    }
}
