package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.service.auth.AuthService;
import roomescape.service.auth.TokenProvider;

@Component
public class AdminAuthHandlerInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final TokenProvider tokenProvider;

    public AdminAuthHandlerInterceptor(AuthService authService, TokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = tokenProvider.extractTokenFromCookie(cookies);
        Member member = authService.findMemberByToken(token);
        if (member == null || !member.getRole().equals(Role.ADMIN)) {
            throw new AuthenticationException("권한이 없습니다.");
        }
        return true;
    }
}
