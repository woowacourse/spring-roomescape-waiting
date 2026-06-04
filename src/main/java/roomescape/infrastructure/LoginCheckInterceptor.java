package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.UnauthenticatedException;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER_ID = "loginUserId";

    private final AuthorizationExtractor<String> authorizationExtractor = new BearerAuthorizationExtractor();
    private final JwtTokenProvider jwtProvider;

    public LoginCheckInterceptor(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String token = authorizationExtractor.extract(request);
        if (token == null) {
            throw new UnauthenticatedException();
        }
        Long userId = jwtProvider.getUserId(token);
        request.setAttribute(LOGIN_USER_ID, userId);
        return true;
    }
}
