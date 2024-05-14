package roomescape.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.auth.provider.CookieProvider;
import roomescape.auth.provider.model.TokenProvider;

@Component
public class TokenResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_NAME = "token";

    private final TokenProvider tokenProvider;

    public TokenResolver(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class);
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();

        String token = CookieProvider.getCookieValue(TOKEN_NAME, httpServletRequest.getCookies());
        return Long.valueOf(tokenProvider.resolveToken(token));
    }
}
