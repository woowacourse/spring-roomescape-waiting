package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.annotation.Authenticated;
import roomescape.auth.exception.MissingAuthorizationHeaderException;

public class NameAuthenticationInterceptor implements HandlerInterceptor {

    private static final String NAME_ATTRIBUTE = "name";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        if (!hm.hasMethodAnnotation(Authenticated.class)) {
            return true;
        }

        String name = extractName(request);
        request.setAttribute(NAME_ATTRIBUTE, name);

        return true;
    }

    private String extractName(HttpServletRequest request) {
        String name = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (name == null || name.isBlank()) {
            throw new MissingAuthorizationHeaderException();
        }

        return name;
    }
}
