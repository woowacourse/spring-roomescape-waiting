package roomescape.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.common.exception.MissingLoginException;
import roomescape.common.exception.NoPermissionException;
import roomescape.member.service.AuthService;
import roomescape.member.service.dto.LoginMemberInfo;

public class AdminRoleInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminRoleInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
