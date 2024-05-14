package roomescape.auth.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class AdminCheckInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AdminCheckInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request, final HttpServletResponse response, final Object handler
    ) {
        String token = extractTokenFromCookies(request.getCookies());
        Member member = authService.findMemberByToken(token);
        if (member == null || !member.getRole().equals(Role.ADMIN)) {
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
