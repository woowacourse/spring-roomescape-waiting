package roomescape.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.api.dto.request.TokenContextRequest;
import roomescape.exception.ForbiddenException;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {
    private final TokenContextRequest tokenContextRequest;

    public CheckAdminInterceptor(final TokenContextRequest tokenContextRequest) {
        this.tokenContextRequest = tokenContextRequest;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final var output = tokenContextRequest.getTokenLoginOutput();
        if (output.isAdmin()) {
            return true;
        }
        throw new ForbiddenException(request.getRequestURI());
    }
}

