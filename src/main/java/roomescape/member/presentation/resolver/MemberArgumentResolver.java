package roomescape.member.presentation.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.global.jwt.AuthorizationExtractor;
import roomescape.global.jwt.CookieAuthorizationExtractor;
import roomescape.global.jwt.TokenProvider;
import roomescape.member.domain.Member;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthorizationExtractor authorizationExtractor;
    private final TokenProvider tokenProvider;

    public MemberArgumentResolver(TokenProvider tokenProvider,
                                  CookieAuthorizationExtractor cookieAuthorizationExtractor) {
        this.tokenProvider = tokenProvider;
        this.authorizationExtractor = cookieAuthorizationExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Long resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = authorizationExtractor.extract(request);
        return tokenProvider.getInfo(token).getId();
    }
}
