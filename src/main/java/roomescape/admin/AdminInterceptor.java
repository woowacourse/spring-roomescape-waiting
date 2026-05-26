package roomescape.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.support.exception.AdminErrorCode;
import roomescape.support.exception.RoomescapeException;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final AdminRequestValidator validator;

    public AdminInterceptor(AdminRequestValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if(validator.isUnauthorized(request)) {
            throw new RoomescapeException(AdminErrorCode.UNAUTHORIZED);
        }
        return true;
    }
}
