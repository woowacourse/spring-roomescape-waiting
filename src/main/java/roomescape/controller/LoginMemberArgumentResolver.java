package roomescape.controller;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.TokenContextRequest;
import roomescape.service.dto.output.TokenLoginOutput;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenContextRequest tokenContextRequest;

    public LoginMemberArgumentResolver(final TokenContextRequest tokenContextRequest) {
        this.tokenContextRequest = tokenContextRequest;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType()
                .equals(LoginMemberRequest.class);
    }

    @Override
    public LoginMemberRequest resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        final TokenLoginOutput output = tokenContextRequest.getTokenLoginOutput();
        return new LoginMemberRequest(output.id(),output.email(), output.password(), output.name());
    }
}
