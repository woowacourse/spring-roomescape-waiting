package roomescape.system.auth.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.system.auth.annotation.Admin;
import roomescape.system.auth.jwt.JwtHandler;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.ForbiddenException;
import roomescape.system.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private final MemberService memberService;
    private final JwtHandler jwtHandler;

    public AdminInterceptor(final MemberService memberService, final JwtHandler jwtHandler) {
        this.memberService = memberService;
        this.jwtHandler = jwtHandler;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    )
            throws Exception {
        if (isHandlerIrrelevantWithAdmin(handler)) {
            return true;
        }

        final Cookie token = getToken(request);
        final Long memberId = jwtHandler.getMemberIdFromToken(token.getValue());
        final Member member = memberService.findMemberById(memberId);

        if (member.isAdmin()) {
            return true;
        }

        throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                String.format("회원 권한이 존재하지 않아 접근할 수 없습니다. [memberId: %d, Role: %s]", member.getId(), member.getRole()));
    }

    private Cookie getToken(final HttpServletRequest request) {
        validateCookieHeader(request);

        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
                .findAny()
                .orElseThrow(() -> new UnauthorizedException(ErrorType.INVALID_TOKEN, "JWT 토큰이 존재하지 않습니다."));
    }

    private void validateCookieHeader(final HttpServletRequest request) {
        final String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new UnauthorizedException(ErrorType.INVALID_TOKEN, "쿠키가 존재하지 않습니다");
        }
    }

    private boolean isHandlerIrrelevantWithAdmin(final Object handler) {
        if (!(handler instanceof final HandlerMethod handlerMethod)) {
            return true;
        }
        final Admin adminAnnotation = handlerMethod.getMethodAnnotation(Admin.class);
        return adminAnnotation == null;
    }
}
