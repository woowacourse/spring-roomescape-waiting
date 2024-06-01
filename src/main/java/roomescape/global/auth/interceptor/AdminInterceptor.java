package roomescape.global.auth.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.constant.JwtKey;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private final MemberRepository memberRepository;
    private final JwtHandler jwtHandler;

    public AdminInterceptor(MemberRepository memberRepository, final JwtHandler jwtHandler) {
        this.memberRepository = memberRepository;
        this.jwtHandler = jwtHandler;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.getMethodAnnotation(Admin.class) == null) {
            return true;
        }

        request.setAttribute(JwtKey.MEMBER_ID.getValue(), parseMemberIdFromRequest(request));
        return true;
    }

    private Long parseMemberIdFromRequest(final HttpServletRequest request) {
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(JwtHandler.ACCESS_TOKEN_HEADER_KEY)) {
                    String accessToken = cookie.getValue();
                    Long memberId = jwtHandler.getMemberIdFromTokenWithValidate(accessToken);
                    Member member = memberRepository.getById(memberId);
                    checkRole(member);

                    return memberId;
                }
            }
        }
        throw new UnauthorizedException(ErrorType.INVALID_TOKEN, ErrorType.INVALID_TOKEN.getDescription());
    }

    private boolean checkRole(final Member member) {
        if (member.isAdmin()) {
            return true;
        }
        throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                String.format("회원 권한이 존재하지 않아 접근할 수 없습니다. [memberId: %d, Role: %s]", member.getId(), member.getRole()));
    }
}
