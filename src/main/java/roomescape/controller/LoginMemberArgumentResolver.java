package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.exception.UnauthorizedException;
import roomescape.service.dto.output.TokenLoginOutput;


@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType()
                .equals(LoginMemberRequest.class);
    }

    @Override
    public LoginMemberRequest resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final TokenLoginOutput output = (TokenLoginOutput) request.getAttribute("member");
        if (output == null) {
            throw new UnauthorizedException();
        }
        return new LoginMemberRequest(output.id(), output.email(), output.password(), output.name());
    }
}
