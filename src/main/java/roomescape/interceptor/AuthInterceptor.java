package roomescape.interceptor;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.dto.UserInfo;
import roomescape.exception.RoomescapeException;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    private final MemberService memberService;

    public AuthInterceptor(TokenService tokenService, MemberService memberService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("token"))
                .limit(1)
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
        long userIdFromToken = tokenService.findUserIdFromToken(token);
        UserInfo userInfo = memberService.findByUserId(userIdFromToken);
        if (!userInfo.role().equals(Role.ADMIN.name())) {
            throw new RoomescapeException(PERMISSION_DENIED);
        }
        return true;
    }
}
