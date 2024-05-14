package roomescape.global.auth.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
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
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Admin adminAnnotation = handlerMethod.getMethodAnnotation(Admin.class);
        if (adminAnnotation == null) {
            return true;
        }

        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME)) {
                    String accessToken = cookie.getValue();
                    Long memberId = jwtHandler.getMemberIdFromTokenWithValidate(accessToken);

                    Member member = memberService.findMemberById(memberId);
                    return checkRole(member, memberId);
                }
            }
        }
        throw new UnauthorizedException(ErrorType.INVALID_TOKEN, "JWT 토큰이 존재하지 않거나 유효하지 않습니다.");
    }

    private boolean checkRole(final Member member, final Long memberId) {
        if (member.isRole(Role.ADMIN)) {
            return true;
        }
        throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                String.format("회원 권한이 존재하지 않아 접근할 수 없습니다. [memberId: %d, Role: %s]", memberId, member.getRole()));
    }
}
