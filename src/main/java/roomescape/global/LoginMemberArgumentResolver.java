package roomescape.global;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import roomescape.domain.Member;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.service.AuthService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public LoginMemberArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpSession session = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class)
                .getSession(false);
        if (session == null) {
            throw unauthenticated();
        }

        Object memberId = session.getAttribute(AuthService.LOGIN_MEMBER_ID);
        if (!(memberId instanceof Long id)) {
            throw unauthenticated();
        }

        return authService.getLoginMember(id);
    }

    private RoomescapeException unauthenticated() {
        return new RoomescapeException(DomainErrorCode.UNAUTHENTICATED, "로그인이 필요합니다.");
    }
}
