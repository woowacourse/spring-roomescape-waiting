package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CheckAuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationExtractor authenticationExtractor;

    public CheckAuthenticationInterceptor(AuthenticationExtractor authenticationExtractor) {
        this.authenticationExtractor = authenticationExtractor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            authenticationExtractor.validateRole(request);
        } catch (SecurityException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }
}
