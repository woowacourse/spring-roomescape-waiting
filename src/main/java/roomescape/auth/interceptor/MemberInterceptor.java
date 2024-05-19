package roomescape.auth.interceptor;

import org.springframework.stereotype.Component;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.service.AuthService;

@Component
public class MemberInterceptor extends AuthInterceptor {

    public MemberInterceptor(RequestHandler requestHandler, AuthService authService) {
        super(requestHandler, authService);
    }

    @Override
    protected boolean isAuthorized(AuthInfo authInfo) {
        return authInfo != null;
    }

}
