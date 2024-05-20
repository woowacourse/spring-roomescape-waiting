package roomescape.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.provider.CookieProvider;
import roomescape.auth.provider.model.TokenProvider;
import roomescape.auth.annotation.Auth;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.role.MemberRole;
import roomescape.member.service.MemberService;

@Component
public class MemberRoleInterceptor implements HandlerInterceptor {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    public MemberRoleInterceptor(MemberService memberService, TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Auth auth = getAuth(handlerMethod);
        if (auth == null) {
            return true;
        }

        String token = CookieProvider.getCookieValue("token", request.getCookies());
        long memberId = Long.parseLong(tokenProvider.resolveToken(token));

        if (!isSameRole(auth, memberId)) {
            throw new RoomEscapeException(MemberExceptionCode.MEMBER_ROLE_UN_AUTHORIZED_EXCEPTION);
        }
        return true;
    }

    private boolean isSameRole(Auth auth, long memberId) {
        MemberRole[] permittedRole = auth.roles();
        return memberService.findMemberRole(memberId).hasSameRoleFrom(permittedRole);
    }

    private Auth getAuth(HandlerMethod handlerMethod) {
        Auth classAuth = handlerMethod.getBeanType().getAnnotation(Auth.class);
        Auth methodAuth = handlerMethod.getMethodAnnotation(Auth.class);

        if (classAuth != null) {
            return classAuth;
        }
        if (methodAuth != null) {
            return methodAuth;
        }
        return null;
    }
}
