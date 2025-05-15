package roomescape.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.error.MissingLoginException;
import roomescape.common.error.NoPermissionException;
import roomescape.member.application.service.AuthService;
import roomescape.member.application.dto.LoginMemberInfo;

public class AdminRoleInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminRoleInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {
        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);
        String token = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("token"))
                .findAny()
                .orElseThrow(MissingLoginException::new)
                .getValue();
        LoginMemberInfo loginMember = authService.getLoginMemberInfoByToken(token);
        if (loginMember.isNotAdmin()) {
            throw new NoPermissionException();
        }
        return true;
    }
}
