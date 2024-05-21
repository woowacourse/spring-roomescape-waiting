package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.controller.member.dto.LoginMember;
import roomescape.infrastructure.TokenExtractor;
import roomescape.service.AuthService;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public LoginMemberArgumentResolver(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public LoginMember resolveArgument(final MethodParameter parameter,
                                       final ModelAndViewContainer mavContainer,
                                       final NativeWebRequest webRequest,
                                       final WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final String token = TokenExtractor.fromRequest(request);
        if (token == null) {
            return null;
        }
        return new LoginMember(authService.findMemberIdByToken(token));
    }
}
