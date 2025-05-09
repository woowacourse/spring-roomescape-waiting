package roomescape.argumentresolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.annotation.Login;
import roomescape.domain.Member;
import roomescape.service.AuthService;

public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private AuthService authService;

    public MemberArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Login.class);
    }

    @Override
    public Member resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // TODO: 토큰이 없을 경우 처리
        String[] cookies = webRequest.getHeader("cookie").split("; ");
        return authService.checkAuthenticationStatus(cookies);
    }
}
