package roomescape.presentation;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.auth.AuthenticationInfo;

public class StubAuthenticationInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationInfo stub;

    public StubAuthenticationInfoArgumentResolver(final AuthenticationInfo stub) {
        this.stub = stub;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(AuthenticationInfo.class);
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer mavContainer,
        final NativeWebRequest webRequest,
        final WebDataBinderFactory binderFactory
    ) {
        return stub;
    }
}
