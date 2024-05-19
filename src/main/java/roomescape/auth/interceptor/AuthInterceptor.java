package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.service.AuthService;
import roomescape.exception.custom.UnauthorizedException;

public abstract class AuthInterceptor implements HandlerInterceptor {
    private final RequestHandler requestHandler;
    private final AuthService authService;

    public AuthInterceptor(RequestHandler requestHandler, AuthService authService) {
        this.requestHandler = requestHandler;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            AuthInfo authInfo = authService.fetchByToken(requestHandler.extract(request));
            if (!isAuthorized(authInfo)) {
                throw new UnauthorizedException("유효하지 않는 토큰입니다.");
            }
        } catch (NullPointerException e) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return true;
    }

    protected abstract boolean isAuthorized(AuthInfo authInfo);

}
