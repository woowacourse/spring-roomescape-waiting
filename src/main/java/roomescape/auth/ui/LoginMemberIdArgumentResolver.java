package roomescape.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.auth.application.TokenProvider;

@Component
@AllArgsConstructor
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenProvider tokenProvider;
    private final CookieProvider cookieProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = cookieProvider.extractTokenFromCookie(request.getCookies());
        return tokenProvider.getMemberId(token);
    }
}
