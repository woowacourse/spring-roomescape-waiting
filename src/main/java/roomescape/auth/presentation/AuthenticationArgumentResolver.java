package roomescape.auth.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.domain.Authenticated;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.exception.AuthenticationRequiredException;
import roomescape.exception.InvalidTokenException;

public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider tokenProvider;
    private final AuthorizationExtractor authorizationExtractor;

    public AuthenticationArgumentResolver(
            JwtTokenProvider tokenProvider,
            AuthorizationExtractor authorizationExtractor
    ) {
        this.tokenProvider = tokenProvider;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(Authenticated.class) != null &&
               parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String token = authorizationExtractor.extract(httpServletRequest)
                .orElseThrow(AuthenticationRequiredException::new);
        String subject = tokenProvider.extractSubject(token);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new InvalidTokenException();
        }
    }
}
