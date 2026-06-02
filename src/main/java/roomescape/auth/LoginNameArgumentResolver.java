package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.annotation.LoginName;
import roomescape.auth.exception.AuthenticationException;

public class LoginNameArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String NAME_ATTRIBUTE = "name";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginName.class);
        boolean isStringType = String.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isStringType;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory)
            throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String name = (String) request.getAttribute(NAME_ATTRIBUTE);

        if (name == null) {
            throw new AuthenticationException();
        }

        return name;
    }
}
