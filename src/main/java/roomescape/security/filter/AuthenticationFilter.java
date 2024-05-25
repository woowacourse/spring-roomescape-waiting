package roomescape.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import roomescape.security.authentication.AnonymousAuthentication;
import roomescape.security.authentication.Authentication;
import roomescape.security.authentication.AuthenticationHolder;
import roomescape.service.AuthService;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHENTICATION_KEY_NAME = "token";

    private final AuthService authService;

    public AuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = createAuthentication(request);
            AuthenticationHolder.setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } finally {
            AuthenticationHolder.clear();
        }
    }

    private Authentication createAuthentication(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, AUTHENTICATION_KEY_NAME);
        if (cookie == null) {
            return new AnonymousAuthentication();
        }
        String token = cookie.getValue();
        return authService.createAuthentication(token);
    }
}
