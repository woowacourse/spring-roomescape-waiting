package roomescape.infra;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.annotation.Auth;
import roomescape.service.TokenService;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenService tokenService;

    public AuthArgumentResolver(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest nativeRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String token = TokenExtractor.extractFrom(nativeRequest.getCookies());
        return tokenService.findMemberIdFromToken(token);
    }
}
