package roomescape.controller.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.AuthorizationException;
import roomescape.service.dto.MemberInfo;
import roomescape.util.CookieManager;
import roomescape.util.TokenManager;

public class CheckAdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieManager.extractAuthCookie(request)
                .orElseThrow(AuthorizationException::new)
                .getValue();
        MemberInfo loginMemberInfo = TokenManager.parse(token);
        if (loginMemberInfo.isNotAdmin()) {
            throw new AuthorizationException();
        }
        return true;
    }
}
