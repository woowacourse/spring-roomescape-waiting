package roomescape.member.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.AuthorizationException;
import roomescape.member.auth.jwt.JwtTokenExtractor;
import roomescape.member.domain.Member;
import roomescape.member.service.AuthService;

@RequiredArgsConstructor
public class AdminPageInterceptor implements HandlerInterceptor {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String token = jwtTokenExtractor.extractTokenFromCookie(request.getCookies());
        final Member member = authService.get(token);

        if (!member.isAdmin()) {
            throw new AuthorizationException();
        }

        return true;
    }
}
