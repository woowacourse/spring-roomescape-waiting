package roomescape.auth.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.core.token.TokenProvider;

public class AuthorizationInterceptor implements HandlerInterceptor {

    private final AuthorizationManager authorizationManager;
    private final TokenProvider tokenProvider;

    public AuthorizationInterceptor(final AuthorizationManager authorizationManager, final TokenProvider tokenProvider) {
        this.authorizationManager = authorizationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        String token = authorizationManager.getAuthorization(request);
        validatedTokeIsBlank(token);
        checkAdminAuthorization(token);
        return true;
    }

    private void validatedTokeIsBlank(final String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityException("회원의 인증 토큰 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
        }
    }

    private void checkAdminAuthorization(final String token) {
        AuthInfo authInfo = tokenProvider.extractAuthInfo(token);
        if (authInfo.isNotAdmin()) {
            throw new SecurityException("관리자 회원이 아닙니다. 관리자 권한이 필요한 기능입니다.");
        }
    }
}
