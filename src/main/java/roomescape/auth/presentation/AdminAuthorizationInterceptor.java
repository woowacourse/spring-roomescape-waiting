package roomescape.auth.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.application.AuthService;
import roomescape.auth.exception.AuthorizationException;
import roomescape.member.domain.Member;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AdminAuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = TokenExtractor.extract(request)
                .orElseThrow(() -> new AuthorizationException("토큰이 존재하지 않습니다."));
        Member member = authService.extractMember(token);
        if (!member.isAdmin()) {
            throw new AuthorizationException("관리자 권한이 없습니다.");
        }
        return true;
    }
}
