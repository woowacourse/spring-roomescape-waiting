package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.util.CookieUtils;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        Cookie[] cookies = request.getCookies();

        if (isPostRequestMembersPath(requestURI, httpMethod)) {
            return true;
        }
        if (cookies == null || CookieUtils.extractTokenFromCookie(cookies) == null) {
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false;
        }
        return true;
    }

    private boolean isPostRequestMembersPath(String requestURI, String httpMethod) {
        return requestURI.equals("/members") && httpMethod.equals("POST");
    }
}
