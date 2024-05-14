package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CheckAuthenticationInterceptor implements HandlerInterceptor {

    private final TokenGenerator tokenGenerator;

    public CheckAuthenticationInterceptor(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = tokenGenerator.getTokenFromCookies(request);

        tokenGenerator.validateTokenRole(token);

        return true;
    }
}
