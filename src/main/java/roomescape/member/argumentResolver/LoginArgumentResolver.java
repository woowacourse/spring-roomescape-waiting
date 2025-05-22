package roomescape.member.argumentResolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.util.TokenCookieManager;
import roomescape.member.service.LoginService;

public class LoginArgumentResolver implements HandlerMethodArgumentResolver {
    private final LoginService loginService;
    private final TokenCookieManager tokenCookieManager;

    public LoginArgumentResolver(LoginService loginService, TokenCookieManager tokenCookieManager) {
        this.loginService = loginService;
        this.tokenCookieManager = tokenCookieManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean hasLoginMemberType = roomescape.member.dto.request.LoginMember.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && hasLoginMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = tokenCookieManager.extractTokenFromCookie(request);
        return loginService.loginCheck(token);
    }
}
