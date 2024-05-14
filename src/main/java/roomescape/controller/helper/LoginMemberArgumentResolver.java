package roomescape.controller.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.Member;
import roomescape.service.LoginService;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final LoginService loginService;
    private final CookieExtractor cookieExtractor;

    public LoginMemberArgumentResolver(LoginService loginService, CookieExtractor cookieExtractor) {
        this.loginService = loginService;
        this.cookieExtractor = cookieExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Member resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = cookieExtractor.getToken(request.getCookies());
        return loginService.findMemberByToken(token);
    }
}
