package roomescape.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.ForbiddenException;
import roomescape.service.dto.output.TokenLoginOutput;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final TokenLoginOutput output = (TokenLoginOutput) request.getAttribute("member");
        if (output != null && output.isAdmin()) {
            return true;
        }
        throw new ForbiddenException(request.getRequestURI());
    }
}

