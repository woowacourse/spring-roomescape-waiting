package roomescape.auth.interceptor;

import org.springframework.stereotype.Component;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.service.AuthService;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Role;

@Component
public class AdminInterceptor extends AuthInterceptor {

    public AdminInterceptor(RequestHandler requestHandler, AuthService authService) {
        super(requestHandler, authService);
    }

    @Override
    protected boolean isAuthorized(AuthInfo authInfo) {
        return authInfo != null && authInfo.getRole().equals(Role.ADMIN);
    }

    @Override
    protected ErrorType getUnauthorizedErrorType() {
        return ErrorType.NOT_ALLOWED_PERMISSION_ERROR;
    }
}
