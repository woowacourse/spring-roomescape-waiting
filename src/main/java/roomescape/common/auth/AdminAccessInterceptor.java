package roomescape.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.user.User;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object loginUser = session.getAttribute(SessionKeys.LOGIN_USER);
        if (!(loginUser instanceof User user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return true;
    }
}
