package roomescape.config.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.config.annotation.RequiredAccessToken;
import roomescape.dto.business.AccessTokenContent;
import roomescape.exception.local.NotFoundCookieException;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final CookieUtility cookieUtility;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginMemberArgumentResolver(CookieUtility cookieUtility, JwtTokenProvider jwtTokenProvider) {
        this.cookieUtility = cookieUtility;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(RequiredAccessToken.class) &&
                methodParameter.getParameterType().isAssignableFrom(AccessTokenContent.class);
    }

    @Override
    public AccessTokenContent resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Cookie accessTokenCookie = getAccessTokenCookie(request);
        return jwtTokenProvider.parseAccessToken(accessTokenCookie.getValue());
    }

    private Cookie getAccessTokenCookie(HttpServletRequest request) {
        return cookieUtility.findCookie(request, "access")
                .orElseThrow(NotFoundCookieException::new);
    }
}
