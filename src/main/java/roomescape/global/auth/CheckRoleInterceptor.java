package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.LoginMember;
import roomescape.global.exception.AuthorizationException;

@Component
public class CheckRoleInterceptor implements HandlerInterceptor {

    private final JwtManager jwtManager;

    public CheckRoleInterceptor(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginMember member = jwtManager.findMember(request);
        if (!member.isAdmin()) {
            throw new AuthorizationException("접근 권한이 없습니다.");
        }
        return true;
    }
}
