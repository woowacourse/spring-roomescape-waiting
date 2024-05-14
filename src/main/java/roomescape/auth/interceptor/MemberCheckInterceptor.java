package roomescape.auth.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.ErrorType;

public class MemberCheckInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public MemberCheckInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {
        String token = extractTokenFromCookies(request.getCookies());
        try {
            authService.findMemberByToken(token);
        } catch (final NoSuchElementException exception) {
            throw new BusinessException(ErrorType.ACCESS_FORBIDDEN);
        }
        return true;
    }

    private String extractTokenFromCookies(final Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            throw new BusinessException(ErrorType.UNAUTHORIZED);
        }
        return Arrays.asList(cookies).stream()
                .filter(cookie -> cookie.getName().equals("token"))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorType.UNAUTHORIZED))
                .getValue();
    }
}
