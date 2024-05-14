package roomescape.config.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.controller.TokenCookieManager;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.service.AuthService;

@Component
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenCookieManager tokenCookieManager;
    private final AuthService authService;

    public AuthenticationArgumentResolver(TokenCookieManager tokenCookieManager, AuthService authService) {
        this.tokenCookieManager = tokenCookieManager;
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoggedInMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = tokenCookieManager.getToken(request.getCookies());
        return authService.findLoggedInMember(token);
    }
}
