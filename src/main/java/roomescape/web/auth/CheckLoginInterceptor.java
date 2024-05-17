package roomescape.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.service.MemberAuthService;
import roomescape.service.response.MemberAppResponse;
import roomescape.web.exception.AuthorizationException;

@Component
public class CheckLoginInterceptor implements HandlerInterceptor {

    private static final String ADMIN = "ADMIN";

    private final MemberAuthService memberAuthService;
    private final JwtProvider jwtProvider;

    public CheckLoginInterceptor(MemberAuthService memberAuthService, JwtProvider jwtProvider) {
        this.memberAuthService = memberAuthService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getCookies() == null) {
            throw new IllegalArgumentException("쿠키가 없습니다. 다시 로그인 해주세요.");
        }
        String token = CookieHandler.extractTokenFromCookies(request.getCookies());
        String email = jwtProvider.getPayload(token);
        MemberAppResponse appResponse = memberAuthService.findMemberByEmail(email);
        if (token == null || !appResponse.role().equals(ADMIN)) {
            throw new AuthorizationException("접근 권한이 없습니다.");
        }

        return true;
    }
}
