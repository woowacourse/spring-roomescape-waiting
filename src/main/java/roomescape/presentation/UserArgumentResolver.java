package roomescape.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.application.AuthenticationService;
import roomescape.application.AuthorizationException;

public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationService authenticationService;

    public UserArgumentResolver(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Authenticated.class);
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer mavContainer,
        final NativeWebRequest webRequest,
        final WebDataBinderFactory binderFactory
    ) {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var tokenCookie = AuthenticationTokenCookie.fromRequest(request);
        if (tokenCookie.hasToken()) {
            return authenticationService.getUserByToken(tokenCookie.token());
        }
        throw new AuthorizationException("사용자 인증이 필요합니다.");
    }
}
