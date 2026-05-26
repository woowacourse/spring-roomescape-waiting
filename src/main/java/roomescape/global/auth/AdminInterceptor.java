package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ADMIN_TOKEN = "ADMIN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (handlerMethod.hasMethodAnnotation(Admin.class) || handlerMethod.getBeanType().isAnnotationPresent(Admin.class)) {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (!ADMIN_TOKEN.equals(authHeader)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
                return false;
            }
        }

        return true;
    }
}
