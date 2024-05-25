package roomescape.global.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.handler.exception.AuthenticationException;
import roomescape.infrastructure.MemberId;
import roomescape.infrastructure.TokenProvider;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenProvider tokenProvider;

    public LoginUserArgumentResolver(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberId.class);
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new AuthenticationException("토큰을 찾을 수 없습니다.");
        }
        return tokenProvider.parseMemberIdFromCookies(request.getCookies());
    }
}
