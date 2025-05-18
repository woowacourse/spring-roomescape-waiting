package roomescape.presentation;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;

public class StubUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final User stubbingUser;

    public StubUserArgumentResolver(final User stubbingUser) {
        this.stubbingUser = stubbingUser;
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
        return stubbingUser;
    }
}
