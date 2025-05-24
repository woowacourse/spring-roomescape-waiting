package roomescape.auth.config;

import static roomescape.exception.SecurityErrorCode.AUTHORITY_NOT_EXIST;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.LoginInfo;
import roomescape.exception.auth.AuthorizationException;

public class AuthorizationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginInfo.class);
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final Object authorization = webRequest.getAttribute("authorization", RequestAttributes.SCOPE_REQUEST);

        if (authorization == null) {
            throw new AuthorizationException(AUTHORITY_NOT_EXIST);
        }

        return authorization;
    }
}
