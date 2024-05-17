package roomescape.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.CookieUtils;
import roomescape.auth.TokenProvider;
import roomescape.exception.InValidRoleException;
import roomescape.member.dto.LoginMemberInToken;

@Component
public class RoleCheckInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    public RoleCheckInterceptor(final TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!hasPermission(request)) {
            throw new InValidRoleException("권한이 없습니다.");
        }
        return true;
    }

    private boolean hasPermission(final HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            String token = CookieUtils.extractTokenFrom(cookies);

            LoginMemberInToken loginMemberInToken = tokenProvider.getLoginMember(token);
            return loginMemberInToken.role().isAdmin();
        } catch (IllegalArgumentException | JwtException e) {
            return false;
        }
    }
}
