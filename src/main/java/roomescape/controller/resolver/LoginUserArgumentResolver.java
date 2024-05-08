package roomescape.controller.resolver;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import roomescape.annotation.AuthenticationPrincipal;
import roomescape.service.AuthService;
import roomescape.service.UserService;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;
    private final UserService userService;

    public LoginUserArgumentResolver(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Long userId = authService.findUserIdByCookie(request.getCookies());
        return userService.findUserById(userId);
    }
}
