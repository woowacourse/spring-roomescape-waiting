package roomescape.member.login.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.AuthorizationException;
import roomescape.common.exception.message.LoginExceptionMessage;
import roomescape.member.domain.Role;

@Component
public class LoginAuthorizationInterceptor implements HandlerInterceptor {

    private final TokenAuthorizationHandler tokenAuthorizationHandler;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginAuthorizationInterceptor(
            TokenAuthorizationHandler tokenAuthorizationHandler,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.tokenAuthorizationHandler = tokenAuthorizationHandler;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Object handler
    ) {
        String token = tokenAuthorizationHandler.extractToken(httpServletRequest);
        if (token == null || token.isBlank()) {
            throw new AuthorizationException(LoginExceptionMessage.INVALID_TOKEN.getMessage());
        }
        String role = jwtTokenProvider.getPayloadRole(token);
        return role.equals(Role.ADMIN.getRole());
    }
}
