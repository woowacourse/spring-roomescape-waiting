package roomescape.controller.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.AuthorizationException;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.util.CookieManager;
import roomescape.util.TokenManager;

public class CheckAdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieManager.extractAuthCookie(request)
                .orElseThrow(AuthorizationException::new)
                .getValue();
        MemberWithoutPassword loginMember = TokenManager.parse(token);
        if (loginMember.isNotAdmin()) {
            throw new AuthorizationException();
        }
        return true;
    }
}
