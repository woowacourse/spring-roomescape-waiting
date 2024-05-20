package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.exception.UnauthorizedException;
import roomescape.util.CookieUtil;

@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String AUTH_COOKIE_NAME = "auth_token";

    private final TokenProvider tokenProvider;

    @Autowired
    public LoginMemberIdArgumentResolver(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return CookieUtil.searchValueFromKey(request.getCookies(), AUTH_COOKIE_NAME)
                .map(tokenProvider::extractMemberId)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다."));
    }
}
