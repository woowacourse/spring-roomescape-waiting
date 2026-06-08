package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.TokenLoginMemberProvider;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.domain.AuthenticatedMember;

@Component
@RequiredArgsConstructor
public class ManagerInterceptor implements HandlerInterceptor {

    private final TokenLoginMemberProvider tokenLoginMemberProvider;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        AuthenticatedMember member = tokenLoginMemberProvider.resolveAndCacheAuthenticatedMember(request);

        if (!member.isManager()) {
            throw new EscapeRoomException(ErrorCode.FORBIDDEN);
        }
        return true;
    }
}
