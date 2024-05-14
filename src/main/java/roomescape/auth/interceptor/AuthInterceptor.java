package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.service.AuthService;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

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
                throw new BusinessException(getUnauthorizedErrorType());
            }
        } catch (BusinessException | NullPointerException e) {
            throw new BusinessException(getSecurityErrorType());
        }
        return true;
    }

    protected abstract boolean isAuthorized(AuthInfo authInfo);

    protected abstract ErrorType getUnauthorizedErrorType();

    protected ErrorType getSecurityErrorType() {
        return ErrorType.SECURITY_EXCEPTION;
    }
}
