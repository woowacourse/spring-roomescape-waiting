package roomescape.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.JwtTokenProvider;
import roomescape.exception.NoAdminPrivilegeException;
import roomescape.member.dto.LoginMember;
import roomescape.util.CookieUtils;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public AdminCheckInterceptor(final JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie[] cookies = request.getCookies();
        String token = CookieUtils.extractTokenFromCookie(cookies);

        LoginMember loginMember = jwtTokenProvider.getMember(token);
        if (!loginMember.role().isAdmin()) {
            throw new NoAdminPrivilegeException("접근 권한이 없습니다.");
        }
        return true;
    }
}
