package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.exception.AuthenticationException;

@RequiredArgsConstructor
public class AuthenticationInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationTokenHandler tokenHandler;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return AuthenticationInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var tokenCookie = AuthenticationTokenCookie.fromRequest(request);
        if (tokenCookie.hasToken()) {
            return tokenHandler.extractAuthenticationInfo(tokenCookie.token());
        }
        throw new AuthenticationException("사용자 인증이 필요합니다.");
    }
}
